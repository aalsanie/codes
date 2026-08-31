package io.github.aalsanie.codes;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class OutcomeRegistry {
    private final Map<OutcomeCode, OutcomeDefinition> definitionsByCode;

    private OutcomeRegistry(Map<OutcomeCode, OutcomeDefinition> definitions) {
        this.definitionsByCode = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
    }

    public static OutcomeRegistry empty() {
        return new OutcomeRegistry(Map.of());
    }

    public static OutcomeRegistry of(OutcomeDefinition... definitions) {
        Objects.requireNonNull(definitions, "definitions");
        return empty().withAll(List.of(definitions));
    }

    public static OutcomeRegistry standard() {
        return empty().withAll(StandardOutcomes.all);
    }

    public int getSize() {
        return definitionsByCode.size();
    }

    public boolean contains(OutcomeCode code) {
        return definitionsByCode.containsKey(Objects.requireNonNull(code, "code"));
    }

    public @Nullable OutcomeDefinition find(OutcomeCode code) {
        return definitionsByCode.get(Objects.requireNonNull(code, "code"));
    }

    public @Nullable OutcomeDefinition find(String value) {
        OutcomeCode code = OutcomeCode.parseOrNull(value);
        return code == null ? null : find(code);
    }

    public OutcomeDefinition require(OutcomeCode code) {
        OutcomeDefinition definition = find(code);
        if (definition == null) {
            throw new NoSuchElementException("unknown outcome code: " + code);
        }
        return definition;
    }

    public List<OutcomeDefinition> definitions() {
        return Collections.unmodifiableList(new ArrayList<>(definitionsByCode.values()));
    }

    public OutcomeRegistry with(OutcomeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definitionsByCode.containsKey(definition.getCode())) {
            throw new IllegalArgumentException("outcome code already registered: " + definition.getCode());
        }
        LinkedHashMap<OutcomeCode, OutcomeDefinition> next = new LinkedHashMap<>(definitionsByCode);
        next.put(definition.getCode(), definition);
        return new OutcomeRegistry(next);
    }

    public OutcomeRegistry withAll(Iterable<OutcomeDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        OutcomeRegistry next = this;
        for (OutcomeDefinition definition : definitions) {
            next = next.with(definition);
        }
        return next;
    }
}
