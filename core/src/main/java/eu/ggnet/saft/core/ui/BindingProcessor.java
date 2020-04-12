/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.ui;

import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.ElementKind.FIELD;

/**
 * Validation Prozesser of the Bind Annotation.
 * Ensures, that the bind Annotation matches the defined Property.
 *
 * @author oliver.guenther
 */
// @SupportedAnnotationTypes("eu.ggnet.saft.core.ui.Bind")
@SupportedAnnotationTypes("eu.ggnet.saft.core.ui.Bind")
@SupportedSourceVersion(RELEASE_8)
public class BindingProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment re) {
        processingEnv.getMessager().printMessage(Kind.NOTE, getClass().getSimpleName() + " processing " + annotations);
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = re.getElementsAnnotatedWith(annotation);
            for (Element annotatedElement : annotatedElements) {
                if ( annotatedElement.getKind() != FIELD )
                    processingEnv.getMessager().printMessage(Kind.ERROR, getClass().getSimpleName() + " Annotation on " + annotatedElement + " not on a field, but on " + annotatedElement.getKind(), annotatedElement);

                if ( annotatedElement.getAnnotation(Bind.class) != null ) {
                    Bind bindAnnotation = annotatedElement.getAnnotation(Bind.class);
                    // Converting Type to TypeMirror. No Better way found yet.
                    TypeMirror classType = processingEnv.getElementUtils().getTypeElement(bindAnnotation.value().allowedClassName()).asType();
                    if ( !processingEnv.getTypeUtils().isSubtype(annotatedElement.asType(), classType) ) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, getClass().getSimpleName()
                                + " allowed fieldClass is " + bindAnnotation.value().allowedClassName()
                                + " but is " + annotatedElement.asType(), annotatedElement);
                    };
                }

//                processingEnv.getMessager().printMessage(Kind.OTHER, BindingProcessor.class.getSimpleName() + " found " + bindAnnotation + " on " + annotatedElement, annotatedElement);
            }
        }
        return true;
    }

}
