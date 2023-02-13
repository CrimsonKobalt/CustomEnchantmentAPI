package db.chris.customenchantment.utils;

public interface LoreFormatter {

    default String format(String loreText) {
        return loreText;
    }

    LoreFormatter DEFAULT = new LoreFormatter(){};
}
