package edu.umass.cs.sase.UI;

import java.io.IOException;

import net.sourceforge.jeval.EvaluationException;
import edu.umass.cs.sase.engine.ConfigFlags;
import edu.umass.cs.sase.engine.EngineController;
import edu.umass.cs.sase.engine.Profiling;
import edu.umass.cs.sase.stream.StreamController;

public class CommandLineUI {
    public static void main(String args[]) throws CloneNotSupportedException, EvaluationException, IOException {
        String nfaFileLocation = "test.query";
        
        if (args.length > 0) {
            nfaFileLocation = args[0];
        }

        StreamController myStreamController = null; 
        EngineController myEngineController = new EngineController();
        myEngineController.setNfa(nfaFileLocation);

        for (int i = 0; i < 20; i++) {
            myEngineController.initializeEngine();
            System.gc();
            System.out.println("\nRepeat No." + (i + 1) + " is started...");

            myStreamController = new StreamController(StockStreamConfig.streamSize, "StockEvent");
            myStreamController.generateStockEventsFromKafka();
            myEngineController.setInput(myStreamController.getMyStream());
            myEngineController.runEngine();

            System.out.println("\nProfiling results for repeat No." + (i + 1) + " are as follows:");
            Profiling.printProfiling();
        }
    }
}
