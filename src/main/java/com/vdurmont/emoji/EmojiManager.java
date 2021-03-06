package com.vdurmont.emoji;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the loaded emojis and provides search functions.
 *
 * @author Vincent DURMONT [vdurmont@gmail.com]
 */
public class EmojiManager {
    private static final String PATH = "/emojis.json";
    private static final Map<String, Emoji> EMOJIS_BY_ALIAS = new HashMap<String, Emoji>();
    private static final Map<String, Set<Emoji>> EMOJIS_BY_TAG = new HashMap<String, Set<Emoji>>();
    private static final Trie EMOJIS_TRIE = new Trie();
    static {
        try {
            InputStream stream = EmojiLoader.class.getResourceAsStream(PATH);
            List<Emoji> emojis = EmojiLoader.loadEmojis(stream);
            for (Emoji emoji : emojis) {
                for (String tag : emoji.getTags()) {
                    if (EMOJIS_BY_TAG.get(tag) == null) {
                        EMOJIS_BY_TAG.put(tag, new HashSet<Emoji>());
                    }
                    EMOJIS_BY_TAG.get(tag).add(emoji);
                }
                for (String alias : emoji.getAliases()) {
                    EMOJIS_BY_ALIAS.put(alias, emoji);
                }
                int stringLength = emoji.getUnicode().length();
                Trie.Node node = EMOJIS_TRIE.getRoot();
                for (int offset = 0; offset < stringLength; ) {
                    final int codePoint = emoji.getUnicode().codePointAt(offset);
                    node.add(codePoint);
                    node = node.getNode(codePoint);
                    offset += Character.charCount(codePoint);
                }
                node.setEnd();
            }
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * No need for a constructor, all the methods are static.
     */
    private EmojiManager() {}

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji}s for a given tag.
     *
     * @param tag the tag
     *
     * @return the associated {@link com.vdurmont.emoji.Emoji}s, null if the tag is unknown
     */
    public static Set<Emoji> getForTag(String tag) {
        if (tag == null) {
            return null;
        }
        return EMOJIS_BY_TAG.get(tag);
    }

    /**
     * Returns the {@link com.vdurmont.emoji.Emoji} for a given alias.
     *
     * @param alias the alias
     *
     * @return the associated {@link com.vdurmont.emoji.Emoji}, null if the alias is unknown
     */
    public static Emoji getForAlias(String alias) {
        if (alias == null) {
            return null;
        }
        return EMOJIS_BY_ALIAS.get(trimAlias(alias));
    }

    private static String trimAlias(String alias) {
        String result = alias;
        if (result.startsWith(":")) {
            result = result.substring(1, result.length());
        }
        if (result.endsWith(":")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji}s
     *
     * @return all the {@link com.vdurmont.emoji.Emoji}s
     */
    public static Collection<Emoji> getAll() {
        return EMOJIS_BY_ALIAS.values();
    }

    /**
     * Tests if a given String is an emoji.
     *
     * @param string the string to test. Can contain multiple emoji
     *
     * @return true if the string is an emoji's unicode, false else
     */
    public static boolean isEmoji(String string) {
        if (string != null) {
            int stringLength = string.length();
            Trie.Node node = EMOJIS_TRIE.getRoot();
            for (int offset = 0; offset < stringLength; ) {
                final int codePoint = string.codePointAt(offset);
                if(!node.nodeContains(codePoint)) {
                    if(node == EMOJIS_TRIE.getRoot())
                        return false;
                    else
                        node = EMOJIS_TRIE.getRoot();
                }else{
                    node = node.getNode(codePoint);
                    offset += Character.charCount(codePoint);
                }
            }
            return node.isEnd();
        }
        return false;
    }

    /**
     * Returns all the tags in the database
     *
     * @return the tags
     */
    public static Collection<String> getAllTags() {
        return EMOJIS_BY_TAG.keySet();
    }
}