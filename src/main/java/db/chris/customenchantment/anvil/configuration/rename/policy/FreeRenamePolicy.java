package db.chris.customenchantment.anvil.configuration.rename.policy;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.rename.RenameCostPolicy;

public class FreeRenamePolicy implements RenameCostPolicy {

    @Override
    public int cost(AnvilMode mode, boolean isRenamed, int previousAnvilUses) {
        return 0;
    }
}
