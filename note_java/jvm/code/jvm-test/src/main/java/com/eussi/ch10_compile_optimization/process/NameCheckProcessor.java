package com.eussi.ch10_compile_optimization.process;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

//������"*"��ʾ֧������Annotations
@SupportedAnnotationTypes("*")
//ֻ֧��JDK 1.7��Java����
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class NameCheckProcessor extends AbstractProcessor {
    private NameChecker nameChecker;

    /**
     * ��ʼ�����Ƽ����
     */
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        nameChecker = new NameChecker(processingEnv);
    }

    /**
     * ��������﷨���ĸ����ڵ�������Ƽ��
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for (Element element : roundEnv.getRootElements())
                nameChecker.checkNames(element);
        }
        return false;
    }
}
