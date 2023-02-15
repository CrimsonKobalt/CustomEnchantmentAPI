package db.chris.customenchantment.anvil.configuration.rename;

import db.chris.customenchantment.anvil.AnvilMode;
import db.chris.customenchantment.anvil.configuration.rename.policy.FreeRenamePolicy;
import db.chris.customenchantment.anvil.configuration.rename.policy.VanillaRenamePolicy;

public interface RenameCostPolicy {

    int cost(AnvilMode mode, boolean isRenamed, int previousAnvilUses);

    RenameCostPolicy VANILLA = new VanillaRenamePolicy();
    RenameCostPolicy FREE = new FreeRenamePolicy();
}
