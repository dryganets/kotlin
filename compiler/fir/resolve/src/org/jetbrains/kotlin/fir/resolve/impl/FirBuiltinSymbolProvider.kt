/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.impl

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.functions.FunctionClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.*
import org.jetbrains.kotlin.fir.deserialization.FirBuiltinAnnotationDeserializer
import org.jetbrains.kotlin.fir.deserialization.FirDeserializationContext
import org.jetbrains.kotlin.fir.deserialization.deserializeClassToSymbol
import org.jetbrains.kotlin.fir.resolve.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.constructClassType
import org.jetbrains.kotlin.fir.resolve.getOrPut
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.KotlinScopeProvider
import org.jetbrains.kotlin.fir.scopes.impl.nestedClassifierScope
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.StandardClassIds
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirResolvedTypeRefImpl
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.builtins.BuiltInsBinaryVersion
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.ProtoBasedClassDataFinder
import org.jetbrains.kotlin.serialization.deserialization.builtins.BuiltInSerializerProtocol
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import java.io.InputStream

class FirBuiltinSymbolProvider(val session: FirSession, val kotlinScopeProvider: KotlinScopeProvider) : FirSymbolProvider() {
    private class BuiltInsPackageFragment(
        stream: InputStream, val fqName: FqName, val session: FirSession,
        val kotlinScopeProvider: KotlinScopeProvider
    ) {
        lateinit var version: BuiltInsBinaryVersion

        val packageProto: ProtoBuf.PackageFragment = run {

            version = BuiltInsBinaryVersion.readFrom(stream)

            if (!version.isCompatible()) {
                // TODO: report a proper diagnostic
                throw UnsupportedOperationException(
                    "Kotlin built-in definition format version is not supported: " +
                            "expected ${BuiltInsBinaryVersion.INSTANCE}, actual $version. " +
                            "Please update Kotlin"
                )
            }

            ProtoBuf.PackageFragment.parseFrom(stream, BuiltInSerializerProtocol.extensionRegistry)
        }

        private val nameResolver = NameResolverImpl(packageProto.strings, packageProto.qualifiedNames)

        val classDataFinder = ProtoBasedClassDataFinder(packageProto, nameResolver, version) { SourceElement.NO_SOURCE }

        private val memberDeserializer by lazy {
            FirDeserializationContext.createForPackage(
                fqName, packageProto.`package`, nameResolver, session,
                FirBuiltinAnnotationDeserializer(session)
            ).memberDeserializer
        }

        val lookup = mutableMapOf<ClassId, FirRegularClassSymbol>()

        fun getClassLikeSymbolByFqName(classId: ClassId): FirRegularClassSymbol? =
            findAndDeserializeClass(classId)

        private fun findAndDeserializeClass(
            classId: ClassId,
            parentContext: FirDeserializationContext? = null
        ): FirRegularClassSymbol? {
            val classIdExists = classId in classDataFinder.allClassIds
            if (!classIdExists) return null
            return lookup.getOrPut(classId, { FirRegularClassSymbol(classId) }) { symbol ->
                val classData = classDataFinder.findClassData(classId)!!
                val classProto = classData.classProto

                deserializeClassToSymbol(
                    classId, classProto, symbol, nameResolver, session,
                    null, kotlinScopeProvider, parentContext,
                    this::findAndDeserializeClass
                )
            }
        }

        fun getTopLevelCallableSymbols(name: Name): List<FirCallableSymbol<*>> {
            return packageProto.`package`.functionList.filter { nameResolver.getName(it.name) == name }.map {
                memberDeserializer.loadFunction(it).symbol
            }
        }

        fun getAllCallableNames(): Set<Name> {
            return packageProto.`package`.functionList.mapTo(mutableSetOf()) { nameResolver.getName(it.name) }
        }

        fun getAllClassNames(): Set<Name> {
            return classDataFinder.allClassIds.mapTo(mutableSetOf()) { it.shortClassName }
        }
    }

    override fun getPackage(fqName: FqName): FqName? {
        if (allPackageFragments.containsKey(fqName)) return fqName
        return null
    }

    private fun loadBuiltIns(): List<BuiltInsPackageFragment> {
        val classLoader = this::class.java.classLoader
        val streamProvider = { path: String -> classLoader?.getResourceAsStream(path) ?: ClassLoader.getSystemResourceAsStream(path) }
        val packageFqNames = KotlinBuiltIns.BUILT_INS_PACKAGE_FQ_NAMES

        return packageFqNames.map { fqName ->
            val resourcePath = BuiltInSerializerProtocol.getBuiltInsFilePath(fqName)
            val inputStream = streamProvider(resourcePath) ?: throw IllegalStateException("Resource not found in classpath: $resourcePath")
            BuiltInsPackageFragment(inputStream, fqName, session, kotlinScopeProvider)
        }
    }

    private val allPackageFragments = loadBuiltIns().groupBy { it.fqName }

    private data class SyntheticFunctionalInterfaceSymbolKey(val kind: FunctionClassDescriptor.Kind, val arity: Int)

    private val syntheticFunctionalInterfaceSymbols = mutableMapOf<SyntheticFunctionalInterfaceSymbolKey, FirRegularClassSymbol>()


    private fun FunctionClassDescriptor.Kind.classId(arity: Int) = ClassId(packageFqName, numberedClassName(arity))

    private fun trySyntheticFunctionalInterface(classId: ClassId): FirRegularClassSymbol? {
        return with(classId) {
            val className = relativeClassName.asString()
            val kind = FunctionClassDescriptor.Kind.byClassNamePrefix(packageFqName, className) ?: return@with null
            val prefix = kind.classNamePrefix
            val arity = className.substring(prefix.length).toIntOrNull() ?: return null
            syntheticFunctionalInterfaceSymbols.getOrPut(SyntheticFunctionalInterfaceSymbolKey(kind, arity)) {
                val status = FirDeclarationStatusImpl(Visibilities.PUBLIC, Modality.ABSTRACT).apply {
                    isExpect = false
                    isActual = false
                    isInner = false
                    isCompanion = false
                    isData = false
                    isInline = false
                }
                FirRegularClassSymbol(this).apply {
                    FirClassImpl(
                        null,
                        session,
                        status,
                        ClassKind.INTERFACE,
                        kotlinScopeProvider,
                        relativeClassName.shortName(),
                        this
                    ).apply klass@{
                        resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                        typeParameters.addAll((1..arity).map {
                            FirTypeParameterImpl(
                                null,
                                this@FirBuiltinSymbolProvider.session,
                                Name.identifier("P$it"),
                                FirTypeParameterSymbol(),
                                Variance.IN_VARIANCE,
                                false
                            ).apply {
                                bounds += session.builtinTypes.nullableAnyType
                            }
                        })
                        typeParameters.add(
                            FirTypeParameterImpl(
                                null,
                                this@FirBuiltinSymbolProvider.session,
                                Name.identifier("R"),
                                FirTypeParameterSymbol(),
                                Variance.OUT_VARIANCE,
                                false
                            ).apply {
                                bounds += session.builtinTypes.nullableAnyType
                            }
                        )
                        val name = OperatorNameConventions.INVOKE
                        val functionStatus = FirDeclarationStatusImpl(Visibilities.PUBLIC, Modality.ABSTRACT).apply {
                            isExpect = false
                            isActual = false
                            isOverride = false
                            isOperator = true
                            isInfix = false
                            isInline = false
                            isTailRec = false
                            isExternal = false
                            isSuspend =
                                kind == FunctionClassDescriptor.Kind.SuspendFunction ||
                                        kind == FunctionClassDescriptor.Kind.KSuspendFunction
                        }
                        val typeArguments = typeParameters.map {
                            FirResolvedTypeRefImpl(
                                null,
                                ConeTypeParameterTypeImpl(it.symbol.toLookupTag(), false)
                            )
                        }

                        addDeclaration(
                            FirSimpleFunctionImpl(
                                null,
                                this@FirBuiltinSymbolProvider.session,
                                typeArguments.last(),
                                null,
                                functionStatus,
                                name,
                                FirNamedFunctionSymbol(CallableId(packageFqName, relativeClassName, name))
                            ).apply {
                                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                                valueParameters += typeArguments.dropLast(1).mapIndexed { index, typeArgument ->
                                    val parameterName = Name.identifier("p${index + 1}")
                                    FirValueParameterImpl(
                                        null,
                                        this@FirBuiltinSymbolProvider.session,
                                        typeArgument,
                                        parameterName,
                                        FirVariableSymbol(parameterName),
                                        defaultValue = null,
                                        isCrossinline = false,
                                        isNoinline = false,
                                        isVararg = false
                                    )
                                }
                            }
                        )

                        fun createSuperType(
                            kind: FunctionClassDescriptor.Kind
                        ): FirResolvedTypeRef {
                            return FirResolvedTypeRefImpl(
                                null,
                                ConeClassLikeLookupTagImpl(kind.classId(arity))
                                    .constructClassType(typeArguments.map { it.type }.toTypedArray(), isNullable = false)
                            )
                        }

                        val superTypes = when (kind) {
                            FunctionClassDescriptor.Kind.Function ->
                                listOf(
                                    FirResolvedTypeRefImpl(
                                        source = null,
                                        ConeClassLikeLookupTagImpl(StandardClassIds.Function)
                                            .constructClassType(arrayOf(typeArguments.last().type), isNullable = false)
                                    )
                                )

                            FunctionClassDescriptor.Kind.SuspendFunction ->
                                listOf(session.builtinTypes.anyType)

                            FunctionClassDescriptor.Kind.KFunction ->
                                listOf(createSuperType(FunctionClassDescriptor.Kind.Function))

                            FunctionClassDescriptor.Kind.KSuspendFunction ->
                                listOf(createSuperType(FunctionClassDescriptor.Kind.SuspendFunction))
                        }
                        replaceSuperTypeRefs(superTypes)
                    }
                }
            }
        }
    }


    override fun getClassLikeSymbolByFqName(classId: ClassId): FirRegularClassSymbol? {
        return allPackageFragments[classId.packageFqName]?.firstNotNullResult {
            it.getClassLikeSymbolByFqName(classId)
        } ?: trySyntheticFunctionalInterface(classId)
    }

    override fun getTopLevelCallableSymbols(packageFqName: FqName, name: Name): List<FirCallableSymbol<*>> {
        return allPackageFragments[packageFqName]?.flatMap {
            it.getTopLevelCallableSymbols(name)
        } ?: emptyList()
    }

    override fun getNestedClassifierScope(classId: ClassId): FirScope? {
        return findRegularClass(classId)?.let {
            nestedClassifierScope(it)
        }
    }

    override fun getAllCallableNamesInPackage(fqName: FqName): Set<Name> {
        return allPackageFragments[fqName]?.flatMapTo(mutableSetOf()) {
            it.getAllCallableNames()
        } ?: emptySet()
    }

    override fun getClassNamesInPackage(fqName: FqName): Set<Name> {
        return allPackageFragments[fqName]?.flatMapTo(mutableSetOf()) {
            it.getAllClassNames()
        } ?: emptySet()
    }

    override fun getAllCallableNamesInClass(classId: ClassId): Set<Name> {
        return getClassDeclarations(classId).mapNotNullTo(mutableSetOf()) {
            when (it) {
                is FirSimpleFunction -> it.name
                is FirVariable<*> -> it.name
                else -> null
            }
        }
    }

    private fun getClassDeclarations(classId: ClassId): List<FirDeclaration> {
        return findRegularClass(classId)?.declarations ?: emptyList()
    }


    private fun findRegularClass(classId: ClassId): FirRegularClass? =
        getClassLikeSymbolByFqName(classId)?.fir

    override fun getNestedClassesNamesInClass(classId: ClassId): Set<Name> {
        return getClassDeclarations(classId).filterIsInstance<FirRegularClass>().mapTo(mutableSetOf()) { it.name }
    }
}
