package org.jetbrains.jet.lang.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.ErrorHandler;
import org.jetbrains.jet.lang.JetDiagnostic;
import org.jetbrains.jet.lang.JetSemanticServices;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.JetDeclaration;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetNamespace;
import org.jetbrains.jet.lang.resolve.java.JavaPackageScope;
import org.jetbrains.jet.lang.resolve.java.JavaSemanticServices;

import java.util.Collections;

/**
 * @author abreslav
 */
public class AnalyzingUtils {
    private final static Key<CachedValue<BindingContext>> BINDING_CONTEXT = Key.create("BINDING_CONTEXT");

    public static BindingContext analyzeFileWithCache(@NotNull final JetFile file) {
        // TODO : Synchronization?
        CachedValue<BindingContext> bindingContextCachedValue = file.getUserData(BINDING_CONTEXT);
        if (bindingContextCachedValue == null) {
            bindingContextCachedValue = CachedValuesManager.getManager(file.getProject()).createCachedValue(new CachedValueProvider<BindingContext>() {
                @Override
                public Result<BindingContext> compute() {
                    JetNamespace rootNamespace = file.getRootNamespace();
                    BindingContext bindingContext = analyzeNamespace(rootNamespace, JetControlFlowDataTraceFactory.EMPTY);
                    return new Result<BindingContext>(bindingContext, PsiModificationTracker.MODIFICATION_COUNT);
                }
            }, false);
            file.putUserData(BINDING_CONTEXT, bindingContextCachedValue);
        }
        return bindingContextCachedValue.getValue();
    }

    public static BindingContext analyzeNamespace(@NotNull JetNamespace namespace, @NotNull JetControlFlowDataTraceFactory flowDataTraceFactory) {
        Project project = namespace.getProject();

        BindingTraceContext bindingTraceContext = new BindingTraceContext();
        JetSemanticServices semanticServices = JetSemanticServices.createSemanticServices(project);
        JavaSemanticServices javaSemanticServices = new JavaSemanticServices(project, semanticServices, bindingTraceContext);

        JetScope libraryScope = semanticServices.getStandardLibrary().getLibraryScope();
        ModuleDescriptor owner = new ModuleDescriptor("<module>");
        final WritableScope scope = new WritableScopeImpl(libraryScope, owner, bindingTraceContext.getErrorHandler(), null);
//        scope.importScope(javaSemanticServices.getDescriptorResolver().resolveNamespace("").getMemberScope());
//        scope.importScope(javaSemanticServices.getDescriptorResolver().resolveNamespace("java.lang").getMemberScope());
        scope.importScope(new JavaPackageScope("", null, javaSemanticServices));
        scope.importScope(new JavaPackageScope("java.lang", null, javaSemanticServices));

        TopDownAnalyzer topDownAnalyzer = new TopDownAnalyzer(semanticServices, bindingTraceContext, flowDataTraceFactory);
//        topDownAnalyzer.process(scope, Collections.<JetDeclaration>singletonList(namespace));
//        if (false)
        topDownAnalyzer.process(scope, new NamespaceLike.Adapter(owner) {

                    @Override
                    public NamespaceDescriptorImpl getNamespace(String name) {
                        return null;
                    }

                    @Override
                    public void addNamespace(@NotNull NamespaceDescriptor namespaceDescriptor) {
                        scope.addNamespace(namespaceDescriptor);
                    }

                    @Override
                    public void addClassifierDescriptor(@NotNull MutableClassDescriptor classDescriptor) {
                        scope.addClassifierDescriptor(classDescriptor);
                    }

                    @Override
                    public void addFunctionDescriptor(@NotNull FunctionDescriptor functionDescriptor) {
                        scope.addFunctionDescriptor(functionDescriptor);
                    }

                    @Override
                    public void addPropertyDescriptor(@NotNull PropertyDescriptor propertyDescriptor) {
                        scope.addVariableDescriptor(propertyDescriptor);
                    }
                }, Collections.<JetDeclaration>singletonList(namespace));
        return bindingTraceContext;
    }

    public static void applyHandler(@NotNull ErrorHandler errorHandler, @NotNull BindingContext bindingContext) {
        for (JetDiagnostic jetDiagnostic : bindingContext.getDiagnostics()) {
            jetDiagnostic.acceptHandler(errorHandler);
        }
    }

    public static void checkForSyntacticErrors(@NotNull PsiElement root) {
        root.acceptChildren(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                element.acceptChildren(this);
            }

            @Override
            public void visitErrorElement(PsiErrorElement element) {
                throw new IllegalArgumentException(element.getErrorDescription() + " at offset " + element.getTextRange().getStartOffset());
            }
        });
    }
}
