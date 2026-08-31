package io.github.aalsanie.codes;

import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class ApiSnapshot {
    private static final List<Class<?>> TYPES = List.of(
        Issue.class,
        MappingResult.class,
        MappingResult.Mapped.class,
        MappingResult.Unmapped.class,
        Outcome.class,
        OutcomeCode.class,
        OutcomeDefinition.class,
        OutcomeException.class,
        OutcomeExceptions.class,
        OutcomeMapper.class,
        OutcomeRegistry.class,
        OutcomeState.class,
        StandardOutcomes.class,
        ValidationResult.class,
        ValidationResult.Valid.class,
        ValidationResult.Invalid.class,
        HttpStatusCode.class,
        HttpOutcomeMapper.class,
        GrpcStatusCode.class,
        GrpcOutcomeMapper.class
    );

    private ApiSnapshot() {
    }

    static String create() {
        return TYPES.stream()
            .sorted(Comparator.comparing(Class::getName))
            .map(ApiSnapshot::typeSnapshot)
            .collect(Collectors.joining("\n\n"));
    }

    private static String typeSnapshot(Class<?> type) {
        List<String> lines = new ArrayList<>();
        lines.add(typeDeclaration(type));

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && !field.isSynthetic()) {
                lines.add("  field " + modifiers(field.getModifiers()) + typeName(field.getGenericType()) + " " + field.getName());
            }
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers()) && !constructor.isSynthetic()) {
                lines.add("  ctor " + modifiers(constructor.getModifiers()) + parameters(constructor.getGenericParameterTypes()));
            }
        }
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && !method.isSynthetic() && !method.isBridge()) {
                lines.add(
                    "  method " + modifiers(method.getModifiers())
                        + typeName(method.getGenericReturnType()) + " " + method.getName()
                        + parameters(method.getGenericParameterTypes())
                );
            }
        }

        String header = lines.remove(0);
        lines.sort(String::compareTo);
        return header + (lines.isEmpty() ? "" : "\n" + String.join("\n", lines));
    }

    private static String typeDeclaration(Class<?> type) {
        String kind;
        if (type.isEnum()) {
            kind = "enum";
        } else if (type.isInterface()) {
            kind = "interface";
        } else {
            kind = "class";
        }

        StringBuilder result = new StringBuilder();
        result.append(modifiers(type.getModifiers())).append(kind).append(' ').append(type.getName());

        if (type.getTypeParameters().length > 0) {
            result.append('<');
            result.append(java.util.Arrays.stream(type.getTypeParameters())
                .map(ApiSnapshot::typeParameter)
                .collect(Collectors.joining(", ")));
            result.append('>');
        }

        if (!type.isInterface() && !type.isEnum()) {
            Type superclass = type.getGenericSuperclass();
            if (superclass != null && superclass != Object.class) {
                result.append(" extends ").append(typeName(superclass));
            }
        }

        Type[] interfaces = type.getGenericInterfaces();
        if (interfaces.length > 0) {
            result.append(type.isInterface() ? " extends " : " implements ");
            result.append(java.util.Arrays.stream(interfaces)
                .map(ApiSnapshot::typeName)
                .sorted()
                .collect(Collectors.joining(", ")));
        }

        if (type.isSealed()) {
            result.append(" permits ");
            result.append(java.util.Arrays.stream(type.getPermittedSubclasses())
                .map(Class::getName)
                .sorted()
                .collect(Collectors.joining(", ")));
        }

        return result.toString();
    }

    private static String typeParameter(TypeVariable<?> variable) {
        Type[] bounds = variable.getBounds();
        if (bounds.length == 1 && bounds[0] == Object.class) {
            return variable.getName();
        }
        return variable.getName() + " extends "
            + java.util.Arrays.stream(bounds).map(ApiSnapshot::typeName).collect(Collectors.joining(" & "));
    }

    private static String modifiers(int value) {
        List<String> values = new ArrayList<>();
        if (Modifier.isPublic(value)) {
            values.add("public");
        }
        if (Modifier.isProtected(value)) {
            values.add("protected");
        }
        if (Modifier.isStatic(value)) {
            values.add("static");
        }
        if (Modifier.isAbstract(value)) {
            values.add("abstract");
        }
        if (Modifier.isFinal(value)) {
            values.add("final");
        }
        return values.isEmpty() ? "" : String.join(" ", values) + " ";
    }

    private static String parameters(Type[] types) {
        return "(" + java.util.Arrays.stream(types).map(ApiSnapshot::typeName).collect(Collectors.joining(", ")) + ")";
    }

    private static String typeName(Type type) {
        return type.getTypeName().replace('$', '.');
    }
}
