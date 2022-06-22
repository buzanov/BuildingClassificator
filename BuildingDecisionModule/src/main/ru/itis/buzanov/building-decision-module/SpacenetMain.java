import java.io.File;

public class SpacenetMain {
	private static final String[] dataSets = new String[] { "3_Paris" };
	private static final int[] minConf = new int[] { 38 };
	public static int numThreads = Runtime.getRuntime().availableProcessors();

	public static void main(String[] args) {
		if (args.length >= 2 && args[0].equalsIgnoreCase("train")) {
			for (int i = 1; i < args.length; i++) {
				String trainingFolder = args[i];
				if (!new File(trainingFolder).exists()) {
					return;
				}
				String dataSet = null;
				for (String s : dataSets) {
					if (trainingFolder.toLowerCase().contains(s.toLowerCase())) {
						dataSet = s;
						break;
					}
				}
				if (dataSet == null) {
					return;
				}
				System.err.println("---TRAINING---");
				System.err.println();
				new BuildingDetectorTrainer().run(trainingFolder, dataSet);
				new PolygonMatcherTrainer().run(trainingFolder, dataSet);
			}
			return;
		} else if (args.length >= 3 && args[0].equalsIgnoreCase("test")) {
			File outputFile = new File(args[args.length - 1]);
			if (outputFile.exists()) outputFile.delete();
			for (int i = 1; i < args.length - 1; i++) {
				String testingFolder = args[i];
				if (!new File(testingFolder).exists()) {
					System.err.println("ERROR: Testing folder not found: " + testingFolder);
					return;
				}
				String dataSet = null;
				int conf = 35;
				for (int j = 0; j < dataSets.length; j++) {
					if (testingFolder.toLowerCase().contains(dataSets[j].toLowerCase())) {
						dataSet = dataSets[j];
						conf = minConf[j];
						break;
					}
				}
				if (dataSet == null) {
					System.err.println("ERROR: Testing folder does not match any known data set: " + testingFolder);
					return;
				}
				System.err.println("TESTING");
				System.err.println();
				new BuildingDetectorTester().run(testingFolder, dataSet, conf, outputFile);
			}
			return;
		}
	}
}