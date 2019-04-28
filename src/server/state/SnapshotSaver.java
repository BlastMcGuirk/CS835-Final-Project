package server.state;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the snapshots saved by clients. All snapshots are stored
 * in a snapshots.txt file, in the following format:
 *
 *      ID <#>
 *      <ID#>:<GraphicalObject>
 *          .
 *          .
 *          .
 *      <ID#>:<GraphicalObject>
 *      ID <#>
 *      <ID#>:<GraphicalObject>
 *          .
 *          .
 *          .
 *      <ID#>:<GraphicalObject>
 *
 * etc.
 */
class SnapshotSaver {
    // File name
    private static final String PATH_TO_SNAPSHOTS = "src/server/state/snapshots.txt";

    // Map of snapshots (mapped ID to List Of GraphicalObjects)
    private ConcurrentHashMap<Long, ArrayList<GraphicalObject>> map;

    SnapshotSaver() {
        map = loadSnapshotsFromFile();
        // Save snapshots to file on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveSnapshotsToFile));
    }

    /**
     * Saves a snapshot to the map
     * @param ID ID of client saving snapshot
     * @param list List of GraphicalObjects at time of snapshot
     */
    void saveSnapshot(long ID, ArrayList<GraphicalObject> list) {
        ArrayList<GraphicalObject> newList = new ArrayList<>(list.size());
        for (GraphicalObject go : list) {
            newList.add(go.clone());
        }
        map.put(ID, newList);
    }

    /**
     * Retrieves a snapshot from the map
     * @param ID ID of snapshot being retrieved
     * @return List of GraphicalObjects saved in snapshot
     */
    ArrayList<GraphicalObject> retrieveSnapshot(long ID) {
        return map.getOrDefault(ID, new ArrayList<>());
    }

    /**
     * Helper method that loads snapshots from file. Creates a new map and
     * fills it with information from the file.
     * @return A map of the snapshots
     */
    private ConcurrentHashMap<Long, ArrayList<GraphicalObject>> loadSnapshotsFromFile() {
        System.out.println("SnapshotSaver: Reading from file...");
        ConcurrentHashMap<Long, ArrayList<GraphicalObject>> newMap = new ConcurrentHashMap<>();
        try {
            // Open stream of file
            FileReader fileReader = new FileReader(PATH_TO_SNAPSHOTS);
            BufferedReader fileLines = new BufferedReader(fileReader);

            // Prepare to get lines
            String line;

            // Prepare a "snapshot"
            long ID = 0;
            ArrayList<GraphicalObject> list = new ArrayList<>();

            // While file has lines
            while ((line = fileLines.readLine()) != null) {
                // New "snapshot"
                if (line.startsWith("ID")) {
                    // Only if it's not the first snapshot
                    if (ID != 0) {
                        newMap.put(ID, list);
                    }

                    // Get new ID and list
                    ID = Long.parseLong(line.substring(3));
                    list = new ArrayList<>();
                }
                // Add GraphicalObject to the list
                else {
                    String[] data = line.split(":");
                    long goID = Long.parseLong(data[0]);
                    list.add(new GraphicalObject(goID, data[1]));
                }
            }
            if (ID != 0) {
                newMap.put(ID, list);
            }
        } catch (Exception e) {
            System.out.println("Cannot read file: " + PATH_TO_SNAPSHOTS);
        }
        return newMap;
    }

    /**
     * Helper method to save snapshots to file on shutdown. It goes through
     * the map and writes each snapshot to file in the format specified in
     * the class JavaDoc above.
     */
    private void saveSnapshotsToFile() {
        System.out.println("SnapshotSaver: Saving to file...");
        try {
            FileWriter fileWriter = new FileWriter(PATH_TO_SNAPSHOTS);
            PrintWriter writer = new PrintWriter(fileWriter);
            // Write each snapshot to file
            map.forEach((id, list) -> {
                // Print ID
                writer.println("ID " + id);
                // Then shape list
                for (GraphicalObject go : list) {
                    writer.println(go.getID() + ":" + go);
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
