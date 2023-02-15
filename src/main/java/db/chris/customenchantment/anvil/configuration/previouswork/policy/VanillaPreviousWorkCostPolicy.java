package db.chris.customenchantment.anvil.configuration.previouswork.policy;

import db.chris.customenchantment.anvil.configuration.previouswork.PreviousWorkCostPolicy;

public class VanillaPreviousWorkCostPolicy implements PreviousWorkCostPolicy {

    @Override
    public int cost(int previousAnvilUses) {
        return (int) (Math.pow(previousAnvilUses, 2) + 1);
    }
}
