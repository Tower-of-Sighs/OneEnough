package cc.sighs.oneenoughitem.util;

import net.neoforged.fml.ModList;

public class JECHCompat {
    public static boolean JECHLoaded = ModList.get().isLoaded("jecharacters");

    public static boolean contains(String chinese, String pinyin) {
        if (JECHLoaded) {
            try {
                return (boolean) Class.forName("me.towdium.jecharacters.utils.Match")
                        .getMethod("contains", String.class, String.class)
                        .invoke(null, chinese, pinyin);
            } catch (ReflectiveOperationException ignored) {
                return chinese.contains(pinyin);
            }
        } else {
            return chinese.contains(pinyin);
        }
    }
}
