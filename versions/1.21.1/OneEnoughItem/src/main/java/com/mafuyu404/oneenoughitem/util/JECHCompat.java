package com.mafuyu404.oneenoughitem.util;

import me.towdium.jecharacters.JustEnoughCharacters;
import me.towdium.jecharacters.utils.Match;
import net.neoforged.fml.ModList;

public class JECHCompat {
    public static boolean JECHLoaded = ModList.get().isLoaded(JustEnoughCharacters.MODID);

    public static boolean contains(String chinese, String pinyin) {
        if (JECHLoaded) {
            return Match.contains(chinese, pinyin);
        } else {
            return chinese.contains(pinyin);
        }
    }
}
