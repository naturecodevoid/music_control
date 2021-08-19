package com.github.charlyb01.music_control.categories;

import com.github.charlyb01.music_control.client.MusicControlClient;
import com.github.charlyb01.music_control.imixin.ISoundSetMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MusicCategories {
    public final static Map<Identifier, Integer> OVERWORLD = new HashMap<>();
    public final static Map<Identifier, Integer> NETHER = new HashMap<>();
    public final static Map<Identifier, Integer> END = new HashMap<>();
    public final static Map<Identifier, Integer> DISC = new HashMap<>();
    public final static Map<Identifier, Integer> CUSTOM = new HashMap<>();
    public final static Map<Identifier, Integer> ALL = new HashMap<>();

    public final static Map<String, Integer> CUSTOM_LIST = new HashMap<>();

    private MusicCategories() {}

    public static void init (final MinecraftClient client) {
        if (MusicControlClient.init) {
            OVERWORLD.clear();
            NETHER.clear();
            END.clear();
            DISC.clear();
            CUSTOM.clear();
            CUSTOM_LIST.clear();
            ALL.clear();
        } else {
            MusicControlClient.init = true;
        }

        for (Identifier identifier : client.getSoundManager().getKeys()) {
            if (client.getSoundManager().get(identifier) != null) {

                Integer soundSize = ((ISoundSetMixin) (Objects.requireNonNull(client.getSoundManager().get(identifier)))).getSoundsSize();
                soundSize = Math.max(soundSize, 1);
                String namespace = "";
                String id = "";
                if (identifier.toString().split(":").length > 1) {
                    namespace = identifier.toString().split(":")[0];
                    id = identifier.toString().split(":")[1];
                }

                if (namespace.equals("minecraft")) {
                    if (id.startsWith("music.game")
                            || id.startsWith("music.creative")
                            || id.startsWith("music.menu")
                            || id.startsWith("music.under_water")) {

                        OVERWORLD.put(identifier, soundSize);
                        ALL.put(identifier, soundSize);

                    } else if (id.startsWith("music.nether")) {

                        NETHER.put(identifier, soundSize);
                        ALL.put(identifier, soundSize);

                    } else if (id.startsWith("music.end")
                            || id.startsWith("music.dragon")
                            || id.startsWith("music.credits")) {

                        END.put(identifier, soundSize);
                        ALL.put(identifier, soundSize);

                    } else if (id.startsWith("music_disc")) {

                        DISC.put(identifier, soundSize);
                        ALL.put(identifier, soundSize);
                    }

                } else {
                    if (id.contains("music")) {
                        if (CUSTOM_LIST.get(namespace) == null) {
                            CUSTOM_LIST.put(namespace, 1);
                        } else {
                            CUSTOM_LIST.put(namespace, CUSTOM_LIST.get(namespace) + 1);
                        }

                        CUSTOM.put(identifier, soundSize);
                        ALL.put(identifier, soundSize);
                    }
                }
            }
        }

        if (!CUSTOM_LIST.isEmpty()) {
            MusicControlClient.currentSubCategory = (String) CUSTOM_LIST.keySet().toArray()[0];
        }
    }

    public static void changeCategory () {
        int current = 0;
        int next;
        for (MusicCategory category: MusicCategory.values()) {
            if (MusicControlClient.currentCategory.equals(category)) {
                break;
            }
            current++;
        }
        next = (current + 1) % MusicCategory.values().length;

        if (MusicCategory.values()[current].equals(MusicCategory.CUSTOM)) {
            if (changeSubCategory()) {
                next = 0;
            } else {
                next = current;
            }
        }

        if (MusicCategory.values()[next].equals(MusicCategory.CUSTOM)) {
            if (CUSTOM_LIST.isEmpty()) {
                next = 0;
            }
        } else if (MusicCategory.values()[next].equals(MusicCategory.ALL)) {
            next = 0;
        }

        MusicControlClient.currentCategory = MusicCategory.values()[next];
    }

    private static boolean changeSubCategory() {
        int current = 0;
        for (String subCategory: CUSTOM_LIST.keySet()) {
            if (MusicControlClient.currentSubCategory.equals(subCategory)) {
                break;
            }
            current++;
        }

        if (current < CUSTOM_LIST.keySet().size()-1) {
            MusicControlClient.currentSubCategory = (String) CUSTOM_LIST.keySet().toArray()[current+1];
            return false;
        } else {
            MusicControlClient.currentSubCategory = (String) CUSTOM_LIST.keySet().toArray()[0];
            return true;
        }
    }

    public static Identifier chooseIdentifier (final Random random) {
        Identifier identifier = null;
        int acc = 0;
        int i = random.nextInt(getCategoryWeight(MusicControlClient.currentCategory));

        for (Map.Entry<Identifier, Integer> entry : MusicControlClient.currentCategory.musics.entrySet()) {
            if (!MusicControlClient.currentCategory.equals(MusicCategory.CUSTOM)
                    || entry.getKey().toString().startsWith(MusicControlClient.currentSubCategory)) {

                acc += entry.getValue();
                if (i < acc) {
                    identifier = entry.getKey();
                    break;
                }
            }
        }

        return identifier;
    }

    private static int getCategoryWeight (final MusicCategory musicCategory) {
        int weight = 0;
        for (Map.Entry<Identifier, Integer> entry : musicCategory.musics.entrySet()) {
            if (!musicCategory.equals(MusicCategory.CUSTOM)
                    || entry.getKey().toString().startsWith(MusicControlClient.currentSubCategory)) {
                weight += entry.getValue();
            }
        }
        return weight;
    }
}
