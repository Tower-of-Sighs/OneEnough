package com.mafuyu404.oneenoughitem.data;

import com.mafuyu404.oelib.api.data.DataDriven;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.stream.Collectors;

@DataDriven(
        folder = "replacements",
        syncToClient = true,
        supportArray = true
)
public record Replacements(
        List<String> match,
        String result,
        Optional<Rules> rules
) {
    private static Set<String> matchKeys() {
        return DomainRegistry.all().values().stream()
                .map(a -> "match" + cap(a.dataId()))
                .collect(Collectors.toSet());
    }

    private static Set<String> resultKeys() {
        return DomainRegistry.all().values().stream()
                .map(a -> "result" + cap(a.dataId()))
                .collect(Collectors.toSet());
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static final Codec<Replacements> CODEC = new Codec<>() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> DataResult<Pair<Replacements, T>> decode(DynamicOps<T> ops, T input) {
            Dynamic<T> dyn = new Dynamic<>(ops, input);

            return dyn.getMapValues().flatMap(map -> {
                List<String> match = null;
                String resultStr = null;
                final Optional<Rules>[] rulesHolder = new Optional[]{Optional.empty()};

                for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : map.entrySet()) {
                    String key = entry.getKey().asString().result().orElse(null);
                    if (key == null) continue;

                    Dynamic<T> valueDyn = entry.getValue();

                    for (String mk : matchKeys()) {
                        if (key.equals(mk)) {
                            match = valueDyn.asStreamOpt()
                                    .map(stream -> stream
                                            .map(d -> d.asString().result().orElse(null))
                                            .filter(Objects::nonNull)
                                            .toList())
                                    .result()
                                    .orElse(Collections.emptyList());
                            break;
                        }
                    }

                    for (String rk : resultKeys()) {
                        if (key.equals(rk)) {
                            resultStr = valueDyn.asString().result().orElse(null);
                            break;
                        }
                    }

                    if ("rules".equals(key)) {
                        Rules.CODEC.decode(ops, valueDyn.getValue())
                                .result()
                                .ifPresent(p -> rulesHolder[0] = Optional.of(p.getFirst()));
                    }
                }

                if (match == null || resultStr == null) {
                    return DataResult.error(() -> "Missing match or result field in Replacements");
                }

                Replacements replacements = new Replacements(match, resultStr, rulesHolder[0]);
                return DataResult.success(Pair.of(replacements, dyn.getValue()));
            });
        }

        @Override
        public <T> DataResult<T> encode(Replacements value, DynamicOps<T> ops, T prefix) {
            try {
                String did = DomainRegistry.currentDataId();
                String mk = "match" + cap(did);
                String rk = "result" + cap(did);

                Map<T, T> map = new LinkedHashMap<>();

                T matchList = ops.createList(
                        value.match().stream()
                                .map(ops::createString)
                );
                map.put(ops.createString(mk), matchList);

                map.put(ops.createString(rk), ops.createString(value.result()));

                value.rules().flatMap(r -> Rules.CODEC.encodeStart(ops, r).result()).ifPresent(encodedRules -> {
                    map.put(ops.createString("rules"), encodedRules);
                });

                return DataResult.success(ops.createMap(map));
            } catch (Exception e) {
                return DataResult.error(() -> "Failed to encode Replacements: " + e.getMessage());
            }
        }
    };

    public enum ProcessingMode {
        REPLACE, RETAIN;
        public static final Codec<ProcessingMode> CODEC = Codec.STRING.flatXmap(
                name -> {
                    try {
                        return DataResult.success(ProcessingMode.valueOf(name.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return DataResult.error(() -> "Invalid processing mode: " + name);
                    }
                },
                mode -> DataResult.success(mode.name().toLowerCase())
        );
    }

    public record Rules(Optional<Map<String, ProcessingMode>> data, Optional<Map<String, ProcessingMode>> tag) {
        public static final Codec<Rules> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.unboundedMap(Codec.STRING, ProcessingMode.CODEC).optionalFieldOf("data").forGetter(Rules::data),
                        Codec.unboundedMap(Codec.STRING, ProcessingMode.CODEC).optionalFieldOf("tag").forGetter(Rules::tag)
                ).apply(instance, Rules::new)
        );
    }
}