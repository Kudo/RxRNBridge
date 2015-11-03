package com.github.kudo.rxrnbridge.compiler;

import com.github.kudo.rxrnbridge.annotations.ReactMethodObservable;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class RxRNBridgeProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private static final String GEN_CLASS_SUFFIX = "$$RxBridge";
    private HashMap<String, Boolean> mDoneClassMap = new HashMap<>();

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(ReactMethodObservable.class.getCanonicalName());
        return types;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(ReactMethodObservable.class)) {
            TypeElement targetClsElement = getTargetClsElement(element);
            if (targetClsElement == null) { continue; }
            generateInjectedClass(targetClsElement);
        }
        return true;
    }

    private TypeElement getTargetClsElement(Element element) {
        if (!SuperficialValidation.validateElement(element)) {
            return null;
        }
        if (element.getKind() != ElementKind.METHOD)  {
            error(element, "Annotation only supports applying to method");
            return null;
        }
        TypeElement targetClsElement = (TypeElement) element.getEnclosingElement();
        if (targetClsElement.getKind() != ElementKind.CLASS) {
            error(targetClsElement, "Parent element should be a class - %s", targetClsElement.getSimpleName());
            return null;
        }
        if (mDoneClassMap.containsKey(targetClsElement.getQualifiedName().toString())) {
            return null;
        }
        mDoneClassMap.put(targetClsElement.getQualifiedName().toString(), true);
        return targetClsElement;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private MethodSpec composeConstructorSpec(List<ExecutableElement> methodElements) {
        // FIXME: Currently only support single parameter constructor with ReactApplicationContext type
        MethodSpec constructorSpec = null;

        for (ExecutableElement methodElement : methodElements) {
            // [0] Find target constructor
            List<? extends VariableElement> params = methodElement.getParameters();
            if (params.size() != 1) { continue; }
            VariableElement param = params.get(0);
            TypeName typeName = TypeName.get(param.asType());
            ClassName demendedClassName = ClassName.get("com.facebook.react.bridge", "ReactApplicationContext");
            if (!typeName.equals(demendedClassName)) { continue; }

            // [1] Construct MethodSpec
            Set<Modifier> paramModifiers = param.getModifiers();
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(typeName, param.getSimpleName().toString())
                    .addModifiers(paramModifiers.toArray(new Modifier[paramModifiers.size()]));
            for (AnnotationMirror annotationMirror : param.getAnnotationMirrors()) {
                paramBuilder.addAnnotation(AnnotationSpec.get(annotationMirror));
            }
            MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
                    .addParameter(paramBuilder.build())
                    .addModifiers(Modifier.PUBLIC);
            methodSpecBuilder.addStatement("super($N)", param.getSimpleName().toString());

            constructorSpec = methodSpecBuilder.build();
        }
        return constructorSpec;
    }

    private ArrayList<MethodSpec> composeAnnotatedMethodSpec(List<ExecutableElement> methodElements) {
        ArrayList<MethodSpec> methodSpecs = new ArrayList<>();
        for (ExecutableElement methodElement : methodElements) {
            if (methodElement.getAnnotation(ReactMethodObservable.class) == null) { continue; }

            // [0] Compose parameters
            List<ParameterSpec> params = new ArrayList<>();
            StringBuilder paramString = new StringBuilder();
            Iterator<? extends VariableElement> iter = methodElement.getParameters().iterator();
            while (iter.hasNext()) {
                VariableElement paramElement = iter.next();
                Set<Modifier> paramModifiers = paramElement.getModifiers();
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.get(paramElement.asType()), paramElement.getSimpleName().toString())
                        .addModifiers(paramModifiers.toArray(new Modifier[paramModifiers.size()]))
                        .build();
                params.add(parameterSpec);
                paramString.append(paramElement.getSimpleName().toString());
                if (iter.hasNext()) { paramString.append(", "); }
            }

            // [1] Append callbacks/promise as last parameter
            // params.add(ParameterSpec.builder(ClassName.get("com.facebook.react.bridge", "Promise"), "promise", Modifier.FINAL).build());
            params.add(ParameterSpec.builder(ClassName.get("com.facebook.react.bridge", "Callback"), "errorCallback", Modifier.FINAL).build());
            params.add(ParameterSpec.builder(ClassName.get("com.facebook.react.bridge", "Callback"), "successCallback", Modifier.FINAL).build());

            // [2] Compose MethodSpec
            MethodSpec methodSpec = MethodSpec.methodBuilder(methodElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ClassName.get("com.facebook.react.bridge", "ReactMethod"))
                    .returns(void.class)
                    .addParameters(params)
                    .addStatement("$T observable = super.$N($N)",
                            TypeName.get(methodElement.getReturnType()),
                            methodElement.getSimpleName().toString(),
                            paramString)
                    // .addStatement("$T.rxRNBridgePromise(observable, promise)",
                    //        ClassName.get("com.github.kudo.rxrnbridge.internal", "RxRNBridgeInternal"))
                    .addStatement("$T.rxRNBridgeCallbacks(observable, errorCallback, successCallback)",
                            ClassName.get("com.github.kudo.rxrnbridge.internal", "RxRNBridgeInternal"))
                    .build();

            methodSpecs.add(methodSpec);
        }
        return methodSpecs;
    }

    private void generateInjectedClass(TypeElement clsElement) {
        List<? extends Element> subElements = clsElement.getEnclosedElements();
        ArrayList<MethodSpec> methodSpecs = new ArrayList<>();

        // [0] Constructor
        MethodSpec constructorSpec = composeConstructorSpec(ElementFilter.constructorsIn(subElements));
        if (constructorSpec == null) {
            error(clsElement, "Declared constructor not found");
        } else {
            methodSpecs.add(constructorSpec);
        }

        // [1] Annotated methods
        methodSpecs.addAll(composeAnnotatedMethodSpec(ElementFilter.methodsIn(subElements)));

        // [2] Package and class
        String packageName = elementUtils.getPackageOf(clsElement).getQualifiedName().toString();
        String injectClsSimpleName = clsElement.getSimpleName() + GEN_CLASS_SUFFIX;

        TypeSpec injectClsSpec = TypeSpec.classBuilder(injectClsSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(clsElement))
                .addMethods(methodSpecs)
                .build();

        // [3] Generate java file
        JavaFile javaFile = JavaFile.builder(packageName, injectClsSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            error(clsElement, "Unable to generate injected class[%s] - %s", clsElement, e.getMessage());
        }
    }
}
