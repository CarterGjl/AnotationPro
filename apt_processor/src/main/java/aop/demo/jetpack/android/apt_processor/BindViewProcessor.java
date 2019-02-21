package aop.demo.jetpack.android.apt_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import aop.demo.jetpack.android.apt_anotation.BindView;
import aop.demo.jetpack.android.utils.ElementUtils;
import aop.demo.jetpack.android.utils.StringUtils;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnvironment.getElementUtils();

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        System.out.println("getCanonicalName"+BindView.class.getCanonicalName());
        set.add(BindView.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        final HashMap<TypeElement, Map<Integer,VariableElement>> variableElementHashMap = new HashMap<>();
        elements.forEach(new Consumer<Element>() {
            @Override
            public void accept(Element element) {
                VariableElement variableElement = (VariableElement) element;
                TypeElement typeEle = (TypeElement) variableElement.getEnclosingElement();
                Map<Integer, VariableElement> elementMap = variableElementHashMap.get(typeEle);
                elementMap = new HashMap<Integer, VariableElement>();
                variableElementHashMap.put(typeEle,elementMap);

                BindView bindView = variableElement.getAnnotation(BindView.class);
                int viewId = bindView.value();
                elementMap.put(viewId,variableElement);
            }
        });
        variableElementHashMap.keySet().forEach(new Consumer<TypeElement>() {
            @Override
            public void accept(TypeElement typeElement) {
                Map<Integer, VariableElement> elementMap = variableElementHashMap.get(typeElement);
                String packageName = ElementUtils.getPackageName(mElementUtils, typeElement);
                JavaFile javaFile = JavaFile.builder(packageName, generateCodeByPoet(typeElement, elementMap)).build();
                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    /**
     *
     * 生成类及类方法
     * @param typeElement
     * @param variableElementMap
     * @return
     */
    private TypeSpec generateCodeByPoet(TypeElement typeElement,Map<Integer,VariableElement>
            variableElementMap){
        //自动生成的文件以 Activity名 + ViewBinding 进行命名
        return TypeSpec.classBuilder(ElementUtils.getEnclosingClassName(typeElement) + "ViewBinding")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethodByPoet(typeElement, variableElementMap))
                .build();
    }
    /**
     * 生成方法
     *
     * @param typeElement        注解对象上层元素对象，即 Activity 对象
     * @param variableElementMap Activity 包含的注解对象以及注解的目标对象
     * @return
     */
    private MethodSpec generateMethodByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {
        ClassName className = ClassName.bestGuess(typeElement.getQualifiedName().toString());
        //方法参数名
        String parameter = "_" + StringUtils.toLowerCaseFirstChar(className.simpleName());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(className, parameter);
        for (int viewId : variableElementMap.keySet()) {
            VariableElement element = variableElementMap.get(viewId);
            //被注解的字段名
            String name = element.getSimpleName().toString();
            //被注解的字段的对象类型的全名称
            String type = element.asType().toString();
            String text = "{0}.{1}=({2})({3}.findViewById({4}));";
            methodBuilder.addCode(MessageFormat.format(text, parameter, name, type, parameter, String.valueOf(viewId)));
        }
        return methodBuilder.build();
    }
}
