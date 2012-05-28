/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.jetbrains.jet.di;

import org.jetbrains.jet.lang.resolve.TopDownAnalyzer;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisContext;
import org.jetbrains.jet.lang.resolve.BodyResolver;
import org.jetbrains.jet.lang.resolve.ControlFlowAnalyzer;
import org.jetbrains.jet.lang.resolve.DeclarationsChecker;
import org.jetbrains.jet.lang.resolve.DescriptorResolver;
import com.intellij.openapi.project.Project;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisParameters;
import org.jetbrains.jet.lang.resolve.ObservableBindingTrace;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;
import org.jetbrains.jet.lang.resolve.java.JavaBridgeConfiguration;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.PsiClassFinderForJvm;
import org.jetbrains.jet.lang.resolve.DeclarationResolver;
import org.jetbrains.jet.lang.resolve.AnnotationResolver;
import org.jetbrains.jet.lang.resolve.calls.CallResolver;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingServices;
import org.jetbrains.jet.lang.resolve.TypeResolver;
import org.jetbrains.jet.lang.resolve.QualifiedExpressionResolver;
import org.jetbrains.jet.lang.resolve.calls.OverloadingConflictResolver;
import org.jetbrains.jet.lang.resolve.ImportsResolver;
import org.jetbrains.jet.lang.resolve.DelegationResolver;
import org.jetbrains.jet.lang.resolve.NamespaceFactoryImpl;
import org.jetbrains.jet.lang.resolve.OverloadResolver;
import org.jetbrains.jet.lang.resolve.OverrideResolver;
import org.jetbrains.jet.lang.resolve.TypeHierarchyResolver;
import org.jetbrains.jet.lang.resolve.ScriptResolver;
import org.jetbrains.jet.lang.resolve.java.JavaSemanticServices;
import org.jetbrains.jet.lang.resolve.java.JavaTypeTransformer;
import com.intellij.openapi.project.Project;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisParameters;
import org.jetbrains.jet.lang.resolve.ObservableBindingTrace;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.annotations.NotNull;
import javax.annotation.PreDestroy;

/* This file is generated by org.jetbrains.jet.di.AllInjectorsGenerator. DO NOT EDIT! */
public class InjectorForTopDownAnalyzerForJvm {

    private TopDownAnalyzer topDownAnalyzer;
    private TopDownAnalysisContext topDownAnalysisContext;
    private BodyResolver bodyResolver;
    private ControlFlowAnalyzer controlFlowAnalyzer;
    private DeclarationsChecker declarationsChecker;
    private DescriptorResolver descriptorResolver;
    private final Project project;
    private final TopDownAnalysisParameters topDownAnalysisParameters;
    private final ObservableBindingTrace observableBindingTrace;
    private final ModuleDescriptor moduleDescriptor;
    private final CompilerDependencies compilerDependencies;
    private CompilerSpecialMode compilerSpecialMode;
    private JavaBridgeConfiguration javaBridgeConfiguration;
    private JavaDescriptorResolver javaDescriptorResolver;
    private PsiClassFinderForJvm psiClassFinderForJvm;
    private DeclarationResolver declarationResolver;
    private AnnotationResolver annotationResolver;
    private CallResolver callResolver;
    private ExpressionTypingServices expressionTypingServices;
    private TypeResolver typeResolver;
    private QualifiedExpressionResolver qualifiedExpressionResolver;
    private OverloadingConflictResolver overloadingConflictResolver;
    private ImportsResolver importsResolver;
    private DelegationResolver delegationResolver;
    private NamespaceFactoryImpl namespaceFactoryImpl;
    private OverloadResolver overloadResolver;
    private OverrideResolver overrideResolver;
    private TypeHierarchyResolver typeHierarchyResolver;
    private ScriptResolver scriptResolver;
    private JavaSemanticServices javaSemanticServices;
    private JavaTypeTransformer javaTypeTransformer;

    public InjectorForTopDownAnalyzerForJvm(
        @NotNull Project project,
        @NotNull TopDownAnalysisParameters topDownAnalysisParameters,
        @NotNull ObservableBindingTrace observableBindingTrace,
        @NotNull ModuleDescriptor moduleDescriptor,
        @NotNull CompilerDependencies compilerDependencies
    ) {
        this.topDownAnalyzer = new TopDownAnalyzer();
        this.topDownAnalysisContext = new TopDownAnalysisContext();
        this.bodyResolver = new BodyResolver();
        this.controlFlowAnalyzer = new ControlFlowAnalyzer();
        this.declarationsChecker = new DeclarationsChecker();
        this.descriptorResolver = new DescriptorResolver();
        this.project = project;
        this.topDownAnalysisParameters = topDownAnalysisParameters;
        this.observableBindingTrace = observableBindingTrace;
        this.moduleDescriptor = moduleDescriptor;
        this.compilerDependencies = compilerDependencies;
        this.compilerSpecialMode = compilerDependencies.getCompilerSpecialMode();
        this.javaBridgeConfiguration = new JavaBridgeConfiguration();
        this.javaDescriptorResolver = new JavaDescriptorResolver();
        this.psiClassFinderForJvm = new PsiClassFinderForJvm();
        this.declarationResolver = new DeclarationResolver();
        this.annotationResolver = new AnnotationResolver();
        this.callResolver = new CallResolver();
        this.expressionTypingServices = new ExpressionTypingServices();
        this.typeResolver = new TypeResolver();
        this.qualifiedExpressionResolver = new QualifiedExpressionResolver();
        this.overloadingConflictResolver = new OverloadingConflictResolver();
        this.importsResolver = new ImportsResolver();
        this.delegationResolver = new DelegationResolver();
        this.namespaceFactoryImpl = new NamespaceFactoryImpl();
        this.overloadResolver = new OverloadResolver();
        this.overrideResolver = new OverrideResolver();
        this.typeHierarchyResolver = new TypeHierarchyResolver();
        this.scriptResolver = new ScriptResolver();
        this.javaSemanticServices = new JavaSemanticServices();
        this.javaTypeTransformer = new JavaTypeTransformer();

        this.topDownAnalyzer.setBodyResolver(bodyResolver);
        this.topDownAnalyzer.setContext(topDownAnalysisContext);
        this.topDownAnalyzer.setControlFlowAnalyzer(controlFlowAnalyzer);
        this.topDownAnalyzer.setDeclarationResolver(declarationResolver);
        this.topDownAnalyzer.setDeclarationsChecker(declarationsChecker);
        this.topDownAnalyzer.setDelegationResolver(delegationResolver);
        this.topDownAnalyzer.setDependencyClassByQualifiedNameResolver(javaDescriptorResolver);
        this.topDownAnalyzer.setModuleDescriptor(moduleDescriptor);
        this.topDownAnalyzer.setNamespaceFactory(namespaceFactoryImpl);
        this.topDownAnalyzer.setOverloadResolver(overloadResolver);
        this.topDownAnalyzer.setOverrideResolver(overrideResolver);
        this.topDownAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
        this.topDownAnalyzer.setTrace(observableBindingTrace);
        this.topDownAnalyzer.setTypeHierarchyResolver(typeHierarchyResolver);

        this.topDownAnalysisContext.setTopDownAnalysisParameters(topDownAnalysisParameters);

        this.bodyResolver.setCallResolver(callResolver);
        this.bodyResolver.setDescriptorResolver(descriptorResolver);
        this.bodyResolver.setExpressionTypingServices(expressionTypingServices);
        this.bodyResolver.setScriptResolver(scriptResolver);
        this.bodyResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
        this.bodyResolver.setTrace(observableBindingTrace);

        this.controlFlowAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
        this.controlFlowAnalyzer.setTrace(observableBindingTrace);

        this.declarationsChecker.setTrace(observableBindingTrace);

        this.descriptorResolver.setAnnotationResolver(annotationResolver);
        this.descriptorResolver.setExpressionTypingServices(expressionTypingServices);
        this.descriptorResolver.setTypeResolver(typeResolver);

        this.javaBridgeConfiguration.setJavaSemanticServices(javaSemanticServices);
        this.javaBridgeConfiguration.setMode(compilerSpecialMode);
        this.javaBridgeConfiguration.setProject(project);

        javaDescriptorResolver.setNamespaceFactory(namespaceFactoryImpl);
        javaDescriptorResolver.setProject(project);
        javaDescriptorResolver.setPsiClassFinder(psiClassFinderForJvm);
        javaDescriptorResolver.setSemanticServices(javaSemanticServices);
        javaDescriptorResolver.setTrace(observableBindingTrace);

        psiClassFinderForJvm.setCompilerDependencies(compilerDependencies);
        psiClassFinderForJvm.setProject(project);

        declarationResolver.setAnnotationResolver(annotationResolver);
        declarationResolver.setContext(topDownAnalysisContext);
        declarationResolver.setDescriptorResolver(descriptorResolver);
        declarationResolver.setImportsResolver(importsResolver);
        declarationResolver.setTrace(observableBindingTrace);

        annotationResolver.setCallResolver(callResolver);
        annotationResolver.setExpressionTypingServices(expressionTypingServices);

        callResolver.setDescriptorResolver(descriptorResolver);
        callResolver.setExpressionTypingServices(expressionTypingServices);
        callResolver.setOverloadingConflictResolver(overloadingConflictResolver);
        callResolver.setTypeResolver(typeResolver);

        expressionTypingServices.setCallResolver(callResolver);
        expressionTypingServices.setDescriptorResolver(descriptorResolver);
        expressionTypingServices.setProject(project);
        expressionTypingServices.setTypeResolver(typeResolver);

        typeResolver.setAnnotationResolver(annotationResolver);
        typeResolver.setDescriptorResolver(descriptorResolver);
        typeResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);

        importsResolver.setConfiguration(javaBridgeConfiguration);
        importsResolver.setContext(topDownAnalysisContext);
        importsResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);
        importsResolver.setTrace(observableBindingTrace);

        delegationResolver.setContext(topDownAnalysisContext);
        delegationResolver.setTrace(observableBindingTrace);

        namespaceFactoryImpl.setConfiguration(javaBridgeConfiguration);
        namespaceFactoryImpl.setModuleDescriptor(moduleDescriptor);
        namespaceFactoryImpl.setTrace(observableBindingTrace);

        overloadResolver.setContext(topDownAnalysisContext);
        overloadResolver.setTrace(observableBindingTrace);

        overrideResolver.setContext(topDownAnalysisContext);
        overrideResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
        overrideResolver.setTrace(observableBindingTrace);

        typeHierarchyResolver.setContext(topDownAnalysisContext);
        typeHierarchyResolver.setDescriptorResolver(descriptorResolver);
        typeHierarchyResolver.setImportsResolver(importsResolver);
        typeHierarchyResolver.setNamespaceFactory(namespaceFactoryImpl);
        typeHierarchyResolver.setScriptResolver(scriptResolver);
        typeHierarchyResolver.setTrace(observableBindingTrace);

        scriptResolver.setContext(topDownAnalysisContext);
        scriptResolver.setDependencyClassByQualifiedNameResolver(javaDescriptorResolver);
        scriptResolver.setExpressionTypingServices(expressionTypingServices);
        scriptResolver.setModuleDescriptor(moduleDescriptor);
        scriptResolver.setNamespaceFactory(namespaceFactoryImpl);
        scriptResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
        scriptResolver.setTrace(observableBindingTrace);

        javaSemanticServices.setDescriptorResolver(javaDescriptorResolver);
        javaSemanticServices.setPsiClassFinder(psiClassFinderForJvm);
        javaSemanticServices.setTrace(observableBindingTrace);
        javaSemanticServices.setTypeTransformer(javaTypeTransformer);

        javaTypeTransformer.setJavaSemanticServices(javaSemanticServices);
        javaTypeTransformer.setResolver(javaDescriptorResolver);

        javaBridgeConfiguration.init();

        psiClassFinderForJvm.initialize();

    }

    @PreDestroy
    public void destroy() {
    }

    public TopDownAnalyzer getTopDownAnalyzer() {
        return this.topDownAnalyzer;
    }

    public TopDownAnalysisContext getTopDownAnalysisContext() {
        return this.topDownAnalysisContext;
    }

    public BodyResolver getBodyResolver() {
        return this.bodyResolver;
    }

    public ControlFlowAnalyzer getControlFlowAnalyzer() {
        return this.controlFlowAnalyzer;
    }

    public DeclarationsChecker getDeclarationsChecker() {
        return this.declarationsChecker;
    }

    public DescriptorResolver getDescriptorResolver() {
        return this.descriptorResolver;
    }

    public Project getProject() {
        return this.project;
    }

    public TopDownAnalysisParameters getTopDownAnalysisParameters() {
        return this.topDownAnalysisParameters;
    }

    public ObservableBindingTrace getObservableBindingTrace() {
        return this.observableBindingTrace;
    }

    public JavaBridgeConfiguration getJavaBridgeConfiguration() {
        return this.javaBridgeConfiguration;
    }

}
