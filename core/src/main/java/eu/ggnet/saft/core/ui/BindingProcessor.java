/*
 * Swing and JavaFx Together (Saft)
 * Copyright (C) 2020  Oliver Guenther <oliver.guenther@gg-net.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 with
 * Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with Classpath Exception along with this program.
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
