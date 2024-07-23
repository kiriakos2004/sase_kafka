package edu.umass.cs.sase.UI;

import java.io.IOException;

import net.sourceforge.jeval.EvaluationException;
import edu.umass.cs.sase.engine.ConfigFlags;
import edu.umass.cs.sase.engine.EngineController;
import edu.umass.cs.sase.engine.Profiling;
import edu.umass.cs.sase.stream.StreamController;

/**
 * The interface
 */
public class CommandLineUI {
    /**
     * The main entry to run the engine under command line
     * 
     * @param args the inputs 
     * 0: the nfa file location 
     * 1: print the results or not (1 for print, 0 for not print)
     * 2: use sharing techniques or not, ("sharingengine" for use, nothing for not use)
     */
    public static void main(String args[]) throws CloneNotSupportedException, EvaluationException, IOException {
        String nfaFileLocation = "test.query";
        int streamSize = 1000; // Define stream size directly
        
        String engineType = null;
        if (args.length > 0) {
            nfaFileLocation = args[0];
        }
        
        if (args.length > 1) {
            if (Integer.parseInt(args[1]) == 1) {
                ConfigFlags.printResults = true;
            } else {
                ConfigFlags.printResults = false;
            }
        }
        
        if (args.length > 2) {
            engineType = args[2];
        }

        StreamController myStreamController = null; 
        
        EngineController myEngineController = new EngineController();
        
        if (engineType != null) {
            myEngineController = new EngineController(engineType);
        }
        myEngineController.setNfa(nfaFileLocation);
        
        for (int i = 0; i < 20; i++) {
            // repeat multiple times for a constant performance
            myEngineController.initializeEngine();
            System.gc();
            System.out.println("\nRepeat No." + (i + 1) + " is started...");
            myStreamController = new StreamController(streamSize, "kafkastockevent");  // Use Kafka for stream
            myEngineController.setInput(myStreamController.getMyStream());
            // myStreamController.printStream();
            myEngineController.runEngine();
            System.out.println("\nProfiling results for repeat No." + (i + 1) + " are as follows:");
            Profiling.printProfiling();
        }
    }
}

