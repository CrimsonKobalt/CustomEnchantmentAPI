package db.chris.customenchantment.utils;

import db.chris.customenchantment.CustomEnchantmentConfig;

import java.util.function.Function;

public class LoreBuilder {

    /**
     * formats the lore using the registered LoreFormatter
     * @param lore e.g. "Power I"
     * @return formatted String to display
     */
    public static String formatLore(String lore) {
        return CustomEnchantmentConfig.get().loreFormatter().format(lore);
    }

    enum RomanNumeral {
        I(1), IV(4), V(5), IX(9), X(10), XL(40), L(50), XC(90), C(100), CD(400), D(500), CM(900), M(1000);
        int weight;

        RomanNumeral(int weight) {
            this.weight = weight;
        }
    };

    public static String toRomanNumeral(long n) {

        if( n <= 0) {
            throw new IllegalArgumentException();
        }

        StringBuilder buf = new StringBuilder();

        final RomanNumeral[] values = RomanNumeral.values();
        for (int i = values.length - 1; i >= 0; i--) {
            while (n >= values[i].weight) {
                buf.append(values[i]);
                n -= values[i].weight;
            }
        }
        return buf.toString();
    }
}
