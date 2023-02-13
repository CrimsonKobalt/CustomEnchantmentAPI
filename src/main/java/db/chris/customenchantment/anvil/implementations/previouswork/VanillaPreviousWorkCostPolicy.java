package db.chris.customenchantment.anvil.implementations.previouswork;

import db.chris.customenchantment.anvil.PreviousWorkCostPolicy;

public class VanillaPreviousWorkCostPolicy implements PreviousWorkCostPolicy {

    @Override
    public int cost(int previousAnvilUses) {
        return (int) (Math.pow(previousAnvilUses, 2) + 1);
    }
}
