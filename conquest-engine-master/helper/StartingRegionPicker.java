package helper;

import bot.BotState;
import main.Region;

import java.util.ArrayList;

/**
 * This class contains method to return preference of starting regions
 */
public class StartingRegionPicker {

    /**
     * Returns the preferred starting regions in organised structure
     * @param state
     * @return list of preferred starting regions
     */
    // TODO: implement this method by setting up preferred starting region list, implemented using domain knowledge
    public static ArrayList<Region> getPreferredStartingRegions(BotState state) {
        ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
        ArrayList<Region> pickableRegions = state.getPickableStartingRegions();

        for (Region region : pickableRegions) {

            //these regions picked because of their bonuses and locations
            switch (region.getId()) {
                case 41:
                    preferredStartingRegions.add(1, region);
                    break;
                case 12:
                    preferredStartingRegions.add(2, region);
                    break;
                case 40:
                    preferredStartingRegions.add(3, region);
                    break;
                case 11:
                    preferredStartingRegions.add(4, region);
                    break;
                case 21:
                    preferredStartingRegions.add(5, region);
                    break;
                case 23:
                    preferredStartingRegions.add(6, region);
                    break;

            }
        }
        return preferredStartingRegions;
    }

}
