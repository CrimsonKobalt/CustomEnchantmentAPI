package db.chris.customenchantment.anvil.configuration.rename.policy;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.rename.RenameCostPolicy;

public class VanillaRenamePolicy implements RenameCostPolicy {

    @Override
    public int cost(AnvilMode mode, boolean isRenamed, int previousAnvilUses) {
        if (! isRenamed ) return 0;
        if (mode != AnvilMode.RENAME) return 1;
        return (int) (Math.pow(previousAnvilUses, 2) + 1);
    }
}
