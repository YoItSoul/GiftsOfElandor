package com.soul.goe.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpellRegistry {
    private static final Map<String, SpellData> SPELLS = new HashMap<>();

    public static void registerSpell(SpellData spellData) {
        SPELLS.put(spellData.getSpellId(), spellData);
    }

    public static Optional<SpellData> getSpell(String spellId) {
        return Optional.ofNullable(SPELLS.get(spellId));
    }

    public static Map<String, SpellData> getAllSpells() {
        return new HashMap<>(SPELLS);
    }
}