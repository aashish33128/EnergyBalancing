import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Protocols {

	double[] energy = null;
	int[][][] meetingInfo = null;
	double totalEnergy = 0;
	int N = PARAMETERS.numNodes;
	int timeOfSimulation = PARAMETERS.timeOfSimulation;
	int numRecords = PARAMETERS.simTime;
	Results rs = new Results();
	Utilities util = null;
	double beta = PARAMETERS.beta;
	double expectedLoss = beta; // Loss aware parameter
	double K = 0;

	public Protocols(double[] energy, int[][][] meetingInfo, double totalenergy2, Results result) {
		this.energy = energy;
		this.meetingInfo = meetingInfo;
		this.totalEnergy = totalenergy2;
		this.rs = result;
		util = new Utilities();
		util.meetingInfo = meetingInfo;

	}

	/**
	 * Protocol Online average: Gathers local information from each interaction
	 * to predict network information. Only allows interaction between nodes on
	 * the opposite sides of average. Splits energy into half.
	 */

	public Results run_POA_Probabilistic() {

		System.out.println("RUNNING POA PROBABILISTIC ALGORITHM");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1.0;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			double predictedEnergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			double target = totalEnergy / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				predictedEnergy = predictedEnergy + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(predictedEnergy);

			}

			String pairs = util.getInteractingPairs(currentTime, 0);

			int first = Integer.parseInt(pairs.split(",")[0]);
			int second = Integer.parseInt(pairs.split(",")[1]);

			avg[first] = ((avg[first] * num[first] + energy[second]) / (num[first] + 1));
			avg[second] = ((avg[second] * num[second] + energy[first]) / (num[second] + 1));

			num[first] = num[first] + 1;
			num[second] = num[second] + 1;

			double x = (energy[first]);
			double y = (energy[second]);

			if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {

				if (x > y) {

					energy[first] = (x + y) / 2; // energyToGive;
					energy[second] = (y + x) / 2 - (beta * (x - y) / 2);
					numInteractions++;

				} else if (y >= x) {

					energy[second] = (x + y) / 2;// energyToGive;
					energy[first] = ((y + x) / 2) - (beta * (y - x) / 2);
					numInteractions++;

				}

			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		/*
		 * //Store Energy Prediction values for(int i=0; i<(N+1); i++) {
		 * rs.setEnergyPrediction(avg[i]); rs.setNumNodesPrediction(num[i]); }
		 */

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());
		return rs;

	}

	/**
	 * Protocol Online average Similar to above but meetings are scheduled by
	 * Mij's
	 */

	public Results run_POA_Mij(double[] energyLevels) {

		System.out.println("RUNNING POA MIJ ALGORITHM");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1.0;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / (N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setEnergyPrediction(sumPrediction / (N + 1));
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);

			int first = Integer.parseInt(pairs.split(",")[0]);
			int second = Integer.parseInt(pairs.split(",")[1]);

			avg[first] = ((avg[first] * num[first] + energy[second]) / (num[first] + 1));
			avg[second] = ((avg[second] * num[second] + energy[first]) / (num[second] + 1));

			num[first] = num[first] + 1;
			num[second] = num[second] + 1;

			/**
			 * Add Global Knowledge
			 */

			
			 // avg[first] = totalenergy/(N+1); avg[second] = totalenergy/(N+1);
			 

			double x = (energy[first]);
			double y = (energy[second]);

			if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {

				if (x > y) {

					energy[first] = (x + y) / 2; // energyToGive;
					energy[second] = (y + x) / 2 - (beta * (x - y) / 2);
					numInteractions++;
					totalenergyex = totalenergyex + (Math.abs(x - y / 2));

				} else if (y >= x) {

					energy[second] = (x + y) / 2;// energyToGive;
					energy[first] = ((y + x) / 2) - (beta * (y - x) / 2);

					numInteractions++;

				}

			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		// Store Energy Prediction values
		for (int i = 0; i < (N + 1); i++) {
			rs.setEnergyPrediction(avg[i]);
			rs.setNumNodesPrediction(num[i]);
		}

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());

		for (int i = 0; i < N + 1; i++) {

			energyLevels = incrementValue(energy[i], energyLevels);
		}
		return rs;

	}

	/**
	 * Multiple Interactions at a time POA MIJ
	 * 
	 * @return
	 */

	public Results run_POA_Mij_Multiple() {

		System.out.println("RUNNING POA MIJ ALGORITHM");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1.0;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setEnergyPrediction(sumPrediction);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);

			}

			ArrayList<String> pairs_multiple = util.getInteractingPairs_multiple(currentTime, 1);

			if (pairs_multiple.size() > 0) {

				for (int pair = 0; pair < pairs_multiple.size(); pair++) {

					String pairs = pairs_multiple.get(0);
					int first = Integer.parseInt(pairs.split(",")[0]);
					int second = Integer.parseInt(pairs.split(",")[1]);

					avg[first] = ((avg[first] * num[first] + energy[second]) / (num[first] + 1));
					avg[second] = ((avg[second] * num[second] + energy[first]) / (num[second] + 1));

					num[first] = num[first] + 1;
					num[second] = num[second] + 1;

					double x = (energy[first]);
					double y = (energy[second]);

					if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {

						if (x > y) {

							energy[first] = (x + y) / 2; // energyToGive;
							energy[second] = (y + x) / 2 - (beta * (x - y) / 2);
							numInteractions++;
							totalenergyex = totalenergyex + (Math.abs(x - y / 2));

						} else if (y >= x) {

							energy[second] = (x + y) / 2;// energyToGive;
							energy[first] = ((y + x) / 2) - (beta * (y - x) / 2);

							numInteractions++;

						}

					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		// Store Energy Prediction values
		for (int i = 0; i < (N + 1); i++) {
			rs.setEnergyPrediction(avg[i]);
			rs.setNumNodesPrediction(num[i]);
		}

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());
		return rs;

	}

	/**
	 * Protocol Online average meetings are scheduled by Mij's, Has global Info.
	 * Comparable to POS
	 */

	public Results run_POA_Mij_Global() {

		System.out.println("RUNNING POA MIJ GLOBAL ALGORITHM");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		double totalenergyex = 0;
		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			double target = totalEnergy / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setEnergyPrediction(totalenergyex);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);

			int first = Integer.parseInt(pairs.split(",")[0]);
			int second = Integer.parseInt(pairs.split(",")[1]);

			double x = (energy[first]);
			double y = (energy[second]);

			if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {

				if (x > y) {

					energy[first] = (x + y) / 2; // energyToGive;
					energy[second] = (y + x) / 2 - (beta * (x - y) / 2);

					totalenergyex = totalenergyex + Math.abs((x - y) / 2);
					numInteractions++;

				} else if (y >= x) {

					energy[second] = (x + y) / 2;// energyToGive;
					energy[first] = ((y + x) / 2) - (beta * (y - x) / 2);
					totalenergyex = totalenergyex + Math.abs((y - x) / 2);
					numInteractions++;

				}

			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		// Store Energy Prediction values
		for (int i = 0; i < (N + 1); i++) {
			rs.setEnergyPrediction(avg[i]);
			rs.setNumNodesPrediction(num[i]);
		}

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());
		return rs;

	}

	/**
	 * Protocol Greedy Transfer Has Global knowledge (for now) Only interacts if
	 * on the opposite sides. transfers required energy from + to - to reach
	 * target distribution for -;
	 */

	public Results run_PGT_global_other() {

		System.out.println("RUNNING PGT Other ALGORITHM");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			double target = totalEnergy / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);

			int first = Integer.parseInt(pairs.split(",")[0]);
			int second = Integer.parseInt(pairs.split(",")[1]);

			double x = (energy[first]);
			double y = (energy[second]);

			if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {

				if (x > y) {

					// Give required energy to node second.
					double eRequired = Math.abs(y - avg[second]);
					eRequired = eRequired + beta * (eRequired); // Loss aware

					if (energy[first] > eRequired) {
						energy[first] = x - eRequired;
						energy[second] = y + ((1 - beta) * eRequired);
						numInteractions++;
					}

				} else if (y >= x) {

					// Give required energy to node second.
					double eRequired = Math.abs(y - avg[second]);
					eRequired = eRequired + beta * (eRequired); // Loss aware

					if (energy[second] > eRequired) {
						energy[second] = y - eRequired;
						energy[first] = x + ((1 - beta) * eRequired);
						numInteractions++;
					}

				}

			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		// Store Energy Prediction values
		for (int i = 0; i < (N + 1); i++) {
			rs.setEnergyPrediction(avg[i]);
			rs.setNumNodesPrediction(num[i]);
		}

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());
		return rs;
	}

	/**
	 * Protocol Greedy Transfer Has Global knowledge (for now) Only interacts if
	 * on the opposite sides. transfers required energy from + to - to reach
	 * target distribution for +;
	 */

	public Results run_PGT_global_self() {

		System.out.println("RUNNING PGT Self ALGORITHM");

		ArrayList<Integer> visited = new ArrayList<>();
		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		int currentTime = 0;
		int numInteractions = 0;

		while (true) {

			currentTime++;
			double sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			sum = 0;
			// double predictedenergy = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy % (N + 1));

				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(0);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);

			int first = Integer.parseInt(pairs.split(",")[0]);
			int second = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			avg[first] = ((avg[first] * num[first] + energy[second]) / (num[first] + 1));
			avg[second] = ((avg[second] * num[second] + energy[first]) / (num[second] + 1));

			num[first] = num[first] + 1;
			num[second] = num[second] + 1;

			// if(!visited.contains(first) && !visited.contains(second))
			{

				double x = (energy[first]);
				double y = (energy[second]);

				// System.out.println(x + ", " + eExcess);
				if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {
					// System.out.println(x + ", " + avg[first] + "," + y + ", "
					// + avg[second]);

					if (x > y) {

						// Give required excess to node second.
						double eExcess = Math.abs(x - avg[first]);

						// eExcess = eExcess + beta * (eExcess); //Doesn't care
						// about loss.

						if (energy[first] > eExcess) {
							energy[first] = x - eExcess;
							System.out.println(energy[first]);
							// visited.add(first);
							energy[second] = y + ((1 - beta) * eExcess);
							numInteractions++;
						}

					} else if (y >= x) {

						// Give required excess to node second.
						double eExcess = Math.abs(y - avg[second]);

						if (energy[second] > eExcess) {
							energy[second] = y - eExcess;
							energy[first] = x + ((1 - beta) * eExcess);
							numInteractions++;
							// visited.add(second);
						}

					}

				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		/*
		 * // Store Energy Prediction values for (int i = 0; i < (N + 1); i++) {
		 * rs.setEnergyPrediction(avg[i]); rs.setNumNodesPrediction(num[i]); }
		 */

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());
		return rs;
	}

	/**
	 * Protocol Greedy Transfer Has Global knowledge (for now) Only interacts if
	 * on the opposite sides. transfers required energy from + to - to reach
	 * target distribution for +;
	 */

	public Results run_PGT_global_self_POA() {

		System.out.println("RUNNING PGT Self ALGORITHM");

		ArrayList<Integer> visited = new ArrayList<>();
		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		int currentTime = 0;
		int numInteractions = 0;

		while (true) {

			currentTime++;
			double sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			sum = 0;
			// double predictedenergy = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy % (N + 1));

				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(0);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);

			int first = Integer.parseInt(pairs.split(",")[0]);
			int second = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			avg[first] = ((avg[first] * num[first] + energy[second]) / (num[first] + 1));
			avg[second] = ((avg[second] * num[second] + energy[first]) / (num[second] + 1));

			num[first] = num[first] + 1;
			num[second] = num[second] + 1;
			
			
			
			 if(!visited.contains(first) && !visited.contains(second))
			{

				double x = (energy[first]);
				double y = (energy[second]);

				// System.out.println(x + ", " + eExcess);
				if ((x > avg[first] && y <= avg[second]) || (x <= avg[first] && y > avg[second])) {
					// System.out.println(x + ", " + avg[first] + "," + y + ", "
					// + avg[second]);

					if (x > y) {

						// Give required excess to node second.
						double eExcess = Math.abs(x - avg[first]);

						// eExcess = eExcess + beta * (eExcess); //Doesn't care
						// about loss.

						if (energy[first] > eExcess) {
							energy[first] = x - eExcess;
							//System.out.println(energy[first]);
							 visited.add(first);
							energy[second] = y + ((1 - beta) * eExcess);
							numInteractions++;
						}

					} else if (y >= x) {

						// Give required excess to node second.
						double eExcess = Math.abs(y - avg[second]);

						if (energy[second] > eExcess) {
							energy[second] = y - eExcess;
							energy[first] = x + ((1 - beta) * eExcess);
							numInteractions++;
							 visited.add(second);
						}

					}

				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		/*
		 * // Store Energy Prediction values for (int i = 0; i < (N + 1); i++) {
		 * rs.setEnergyPrediction(avg[i]); rs.setNumNodesPrediction(num[i]); }
		 */

		System.out.println("--------COMPLETE------------");
		// System.out.println("Size is " + rs.getvD().size());
		return rs;
	}

	public Results ProtocolPGIMD(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(totalenergyex);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] <= imd[j][i]) {

				if (delta1 > 0 && delta2 < 0) {
					if (energy[i] > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if (delta2 > 0 && delta1 < 0) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1 * (1 - beta));
						energy[j] = y - Math.abs(delta1);
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > imd[j][i]) {
				if (delta1 > 0 && delta2 < 0) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2);
						energy[j] = y + Math.abs(delta2 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if (delta2 > 0 && delta1 < 0) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;
	}

	public Results ProtocolPGIMD_LossAware(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(totalenergyex);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] <= imd[j][i]) {

				if (delta1 > 0 && delta2 < 0) {
					if (energy[i] > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if (delta2 > 0 && delta1 < 0) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1);
						energy[j] = y - Math.abs(delta1) - (beta * Math.abs(delta1));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > imd[j][i]) {
				if (delta1 > 0 && delta2 < 0) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2) - (beta * delta2);
						energy[j] = y + Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if (delta2 > 0 && delta1 < 0) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;
	}

	public Results ProtocolPGIMD_Threshold(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD Threshold------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			double target = totalEnergy / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(totalenergyex);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] <= (imd[j][i] + (PARAMETERS.threshold * N))) {

				if (delta1 > 0 && delta2 < 0) {
					if (energy[i] > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if (delta2 > 0 && delta1 < 0) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1 * (1 - beta));
						energy[j] = y - Math.abs(delta1);
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > (imd[j][i] + (PARAMETERS.threshold * N))) {
				if (delta1 > 0 && delta2 < 0) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2);
						energy[j] = y + Math.abs(delta2 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if (delta2 > 0 && delta1 < 0) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;
	}

	public Results ProtocolPGIMD_Casewise(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD CASE WISE------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = (N + 1);
			avg[i] = totalEnergy / (N + 1);
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			double target = totalEnergy / (N + 1);
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(totalenergyex);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			// Cases: + -

			if (delta1 > 0 && delta2 < 0 || (delta1 < 0) && (delta2 > 0)) {
				if (imd[i][j] <= imd[j][i]) {

					if (delta1 > 0 && delta2 < 0) {
						if (energy[i] > delta1) {
							energy[i] = x - (delta1);
							energy[j] = y + (delta1 * (1 - beta));
							totalenergyex = totalenergyex + delta1;
							numInteractions++;
						}

					} else if (delta1 < 0 && delta2 > 0) {
						if (energy[j] > Math.abs(delta1)) {
							energy[i] = x + Math.abs(delta1 * (1 - beta));
							energy[j] = y - Math.abs(delta1);
							totalenergyex = totalenergyex + delta1;
							numInteractions++;
						}
					}
				} else if (imd[i][j] > imd[j][i]) {
					if (delta1 > 0 && delta2 < 0) {
						if (energy[i] > Math.abs(delta2)) {
							energy[i] = x - Math.abs(delta2);
							energy[j] = y + Math.abs(delta2 * (1 - beta));
							totalenergyex = totalenergyex + delta2;
							numInteractions++;

						}
					} else if (delta2 > 0 && delta1 < 0) {
						if (energy[j] > Math.abs(delta2)) {
							energy[i] = x + Math.abs(delta2 * (1 - beta));
							energy[j] = y - Math.abs(delta2);
							totalenergyex = totalenergyex + delta2;
							numInteractions++;
						}
					}
				}

			}

			// Case ++
			if (delta1 > 0 && delta2 > 0) {
				if (imd[i][j] <= imd[j][i]) {
					if (energy[i] > delta1) {
						// Transfer energy so that the node with lower imd
						// reaches target distribution

						energy[i] = energy[i] - delta1;
						energy[j] = energy[j] + ((1 - beta) * delta1);
						totalenergyex = totalenergyex + delta1;
						numInteractions++;
					}
				}

				else if (imd[i][j] > imd[j][i]) {
					if (energy[j] > delta2) {
						energy[j] = energy[j] - delta2;
						energy[i] = energy[i] + ((1 - beta) * delta2);
						totalenergyex = totalenergyex + delta2;
						numInteractions++;
					}
				}

			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;
	}

	public Results protocolIMD_POA(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];
		double variation = 0;
		ArrayList<Integer> visited = new ArrayList<>();

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / ((double) N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / ((double) N + 1));

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			if (!visited.contains(i) && !visited.contains(j)) {

				// Calculate delta
				avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
				avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));

				num[i] = num[i] + 1;
				num[j] = num[j] + 1;

				/**
				 * Add Global Knowledge
				 */
				/*
				 * avg[i] = totalenergy/(N+1); avg[j] = totalenergy/(N+1);
				 */
				double delta1 = (energy[i] - avg[i]);

				double delta2 = (energy[j] - avg[j]);
				double x = energy[i];
				double y = energy[j];

				if (imd[i][j] <= imd[j][i]) {

					if (((x > avg[i] + variation) && (y <= avg[j] - variation))) {
						if (x > delta1) {
							energy[i] = x - (delta1);
							energy[j] = y + (delta1 * (1 - beta));
							totalenergyex = totalenergyex + (Math.abs(delta1));
							visited.add(i);
							numInteractions++;

						}
					} else if ((x <= avg[i] - variation && y > avg[j] + variation)) {
						if (energy[j] > Math.abs(delta1)) {
							energy[i] = x + Math.abs(delta1);
							energy[j] = y - Math.abs(delta1) - (beta * Math.abs(delta1));
							totalenergyex = totalenergyex + (Math.abs(delta1));
							visited.add(i);
							numInteractions++;
						}
					}

				} else if (imd[i][j] > imd[j][i]) {
					if ((x > avg[i] + variation && y <= avg[j] - variation)) {
						if (energy[i] > Math.abs(delta2)) {
							energy[i] = x - Math.abs(delta2) - (beta * Math.abs(delta2));
							energy[j] = y + Math.abs(delta2);
							totalenergyex = totalenergyex + (Math.abs(delta2));
							visited.add(j);
							numInteractions++;

						}
					} else if ((x <= avg[i] - variation && y > avg[j] + variation)) {
						if (energy[j] > Math.abs(delta2)) {
							energy[i] = x + Math.abs(delta2 * (1 - beta));
							energy[j] = y - Math.abs(delta2);
							totalenergyex = totalenergyex + (Math.abs(delta2));
							visited.add(j);
							numInteractions++;
						}
					}
				}
			}
			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;

	}

	public Results protocolIMD_POA_restricted(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];
		double variation = 0;
		ArrayList<Integer> visited = new ArrayList<>();

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / ((double) N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / ((double) N + 1));

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);
			//System.out.println(i + ", " + j);
			

				// Calculate delta
				avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
				avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));

				num[i] = num[i] + 1;
				num[j] = num[j] + 1;

				/**
				 * Add Global Knowledge
				 */
				/*
				 *
				 */
				
			// avg[i] = totalenergy/(N+1); avg[j] = totalenergy/(N+1);
				 
				 if (!visited.contains(i) && !visited.contains(j)) {
				double delta1 = Math.abs(energy[i] - avg[i]);

				double delta2 = Math.abs(energy[j] - avg[j]);

				double x = energy[i];
				double y = energy[j];

				if (x > avg[i] && y <= avg[j]) {
					
					if (imd[i][j] < imd[j][i]) {
						energy[i] = x - delta1;
						energy[j] = y + (1 - beta) * delta1;
						totalenergyex = totalenergyex + delta1;
						visited.add(i);
						numInteractions++;
					} else {
						double energytogive = 0;
						if (x > (delta2 * 1.25)) {
							energytogive = delta2 * 1.25;
							energy[i] = x - energytogive;
							energy[j] = y + (1 - beta) * energytogive;
							totalenergyex = totalenergyex + delta2;
							visited.add(j);
							numInteractions++;
						}
						// added restriction. Even if x doesnt have sufficient
						// energy, give whatever it can.Dont add it to visited.
						else {
							energytogive = 0; // give all energy whatever it
												// has. (Can increase energy
												// loss).We will see
						}

						
					//	numInteractions++;

					}
				} else if (x <= avg[i] && y > avg[j]) {
					
					if (imd[j][i] < imd[i][j]) {
						energy[j] = y - delta2;
						energy[i] = x + (1 - beta) * delta2;
						visited.add(j);
						numInteractions++;
						totalenergyex = totalenergyex + delta2;
					} else {
						double energytogive = 0;
						if (y > delta1 * 1.25) {
							energytogive =  delta1 * 1.25;
							energy[j] = y - energytogive;
							energy[i] = x + (1 - beta) * energytogive;
							visited.add(i);
							totalenergyex = totalenergyex + delta1;
							numInteractions++;
						}
						// added restriction. Even if x doesnt have sufficient
						// energy, give whatever it can.Dont add it to visited.
						else {
							energytogive = 0; // give all energy whatever it
												// has. (Can increase energy
												// loss).We will see
						}

					
						//numInteractions++;
				}

				
			
		

		}
			}
			
			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		return rs;

	}
	
	
	/***
	 * 
	 * 
	 * @param imd
	 * @return
	 */
	
	
	public Results protocolIMD_POA_restricted_hybrid(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];
		double variation = 0;
		ArrayList<Integer> visited = new ArrayList<>();

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / ((double) N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / ((double) N + 1));

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);
			System.out.println(i + ", " + j);
			if (!visited.contains(i) && !visited.contains(j)) {

				// Calculate delta
				avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
				avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));

				num[i] = num[i] + 1;
				num[j] = num[j] + 1;

				/**
				 * Add Global Knowledge
				 */
				/*
				 * avg[i] = totalenergy/(N+1); avg[j] = totalenergy/(N+1);
				 */
				double delta1 = Math.abs(energy[i] - avg[i]);

				double delta2 = Math.abs(energy[j] - avg[j]);

				double x = energy[i];
				double y = energy[j];

				if (x > avg[i] && y <= avg[j]) {
					
					if (imd[i][j] < imd[j][i]) {
						energy[i] = x - delta1;
						energy[j] = y + (1 - beta) * delta1;
						totalenergyex = totalenergyex + delta1;
						visited.add(i);
						numInteractions++;
					

					}
					else
					{
						double total = (x + y)/2;
						energy[i] = total;
						energy[j] = total;
						if(x > total)
						{
							energy[j] = energy[j] - beta * (x - total);
						}
						else
						{
							energy[i] = energy[i] - beta * (y - total);
						}
					}
				} else if (x <= avg[i] && y > avg[j]) {
					
					if (imd[j][i] < imd[i][j]) {
						energy[j] = y - delta2;
						energy[i] = x + (1 - beta) * delta2;
						visited.add(j);
						numInteractions++;
						totalenergyex = totalenergyex + delta2;
					} 
				
				else
				{
					double total = (x + y)/2;
					energy[i] = total;
					energy[j] = total;
					if(x > total)
					{
						energy[j] = energy[j] - beta * (x - total);
					}
					else
					{
						energy[i] = energy[i] - beta * (y - total);
					}
				}
			}
			
			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}
		}

		return rs;

	}
	
	public Results protocolIMD_POA_restricted_K_window(double[][] imd,int K) {
		System.out.println("---STARTING GREEDY WITH IMD POA------");
		this.K = K;
		ArrayList<Integer> visited = new ArrayList<>();
		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		Memory[] memory = new Memory[N + 1];

		// Need to initialize

		for (int i = 0; i < N + 1; i++) {
			memory[i] = new Memory();
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				sumPrediction = sumPrediction + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / (double) (N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / (double) (N + 1));

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);
			

			// Add to memory.

			memory[i].addToMemory(j, energy[j], currentTime, K);
			memory[i].addToMemory(i, energy[i], currentTime, K);

			memory[j].addToMemory(i, energy[i], currentTime, K);
			memory[j].addToMemory(j, energy[j], currentTime, K);

			memory[i].syncMemory(memory[j], i, K);
			// System.out.println("I is " + memory[i].energy_register.size());

			memory[j].syncMemory(memory[i], j, K);
			// System.out.println("J is " + memory[j].energy_register.size());

			avg[i] = memory[i].getAverage();
			avg[j] = memory[j].getAverage();
				/**
				 * Add Global Knowledge
				 */
				/*
				 * avg[i] = totalenergy/(N+1); avg[j] = totalenergy/(N+1);
				 */
			if (!visited.contains(i) && !visited.contains(j)) {
				double delta1 = Math.abs(energy[i] - avg[i]);

				double delta2 = Math.abs(energy[j] - avg[j]);

				double x = energy[i];
				double y = energy[j];

				if (x > avg[i] && y <= avg[j]) {
					
					if (imd[i][j] < imd[j][i]) {
						energy[i] = x - delta1;
						energy[j] = y + (1 - beta) * delta1;
						totalenergyex = totalenergyex + delta1;
						visited.add(i);
						numInteractions++;
					} 
					else {
						double energytogive = 0;
						if (x > delta2 * 1.25) {
							energytogive = delta2 * 1.25;
							energy[i] = x - energytogive;
							energy[j] = y + (1 - beta) * energytogive;
							totalenergyex = totalenergyex + delta2;
							visited.add(j);
							numInteractions++;
						}
						// added restriction. Even if x doesnt have sufficient
						// energy, give whatever it can.Dont add it to visited.
						else {
							energytogive = 0; // give all energy whatever it
												// has. (Can increase energy
												// loss).We will see
						}

						
					//	numInteractions++;

					}
				} else if (x <= avg[i] && y > avg[j]) {
					
					if (imd[j][i] < imd[i][j]) {
						energy[j] = y - delta2;
						energy[i] = x + (1 - beta) * delta2;
						visited.add(j);
						numInteractions++;
						totalenergyex = totalenergyex + delta2;
					} else {
						double energytogive = 0;
						if (y > delta1 * 1.25) {
							energytogive =  delta1 * 1.25;
							energy[j] = y - energytogive;
							energy[i] = x + (1 - beta) * energytogive;
							visited.add(i);
							totalenergyex = totalenergyex + delta1;
							numInteractions++;
						}
						// added restriction. Even if x doesnt have sufficient
						// energy, give whatever it can.Dont add it to visited.
						else {
							energytogive = 0; // give all energy whatever it
												// has. (Can increase energy
												// loss).We will see
						}

					
						//numInteractions++;
				}

				
			
		

		}
			}
			
			if (currentTime > (timeOfSimulation)) {

				break;
			}
		}

		return rs;

	}

	public Results protocolIMD_WeightedPOA(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;
			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];

				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / ((double) N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / ((double) N + 1));

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
			avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));

			num[i] = num[i] + 1;
			num[j] = num[j] + 1;

			/*
			 * double delta1 = energy[i]/imd[i][j] -
			 * (energy[j]/imd[j][i]);//((energy[i]*imd[j][i] - (imd[i][j] *
			 * energy[j])))/(imd[i][j] + imd[j][i]); System.out.println(delta1);
			 */
			// double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			double tenergy = x + y;
			double x_get = (imd[i][j] * tenergy) / (int) (imd[j][i] + imd[i][j]);
			// System.out.println(x + ", " + (int)x_get);

			double y_get = (imd[i][j] * tenergy) / (int) (imd[j][i] + imd[i][j]);
			// System.out.println(y + ", " + y_get);

			/*
			 * if(x_get + y_get > tenergy) { System.out.println((x_get+y_get) +
			 * " , " + tenergy) ; System.exit(0); }
			 * 
			 */
			if (x >= x_get && y < y_get) {
			
				energy[i] = x_get;
				energy[j] = y_get - (beta * (x - x_get));
				numInteractions++;
			} else if (x < x_get && y >= y_get) {
				energy[j] = y_get;
				energy[i] = x_get - (beta * (y - y_get));
				numInteractions++;
				// System.out.println(y_get);
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;

	}

	public Results protocolIMD_POA_average(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA average------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];

				sumPrediction = sumPrediction + num[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			// avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
			// avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));
			// avg[i] = (avg[i] + avg[j] )/(num[i]+num[j]);
			// avg[j] = (avg[i] + avg[j] )/(num[i]+num[j]);
			avg[i] = (avg[i] * num[i] + avg[j] * num[j]) / (num[i] + num[j]);
			avg[j] = (avg[i] * num[i] + avg[j] * num[j]) / (num[i] + num[j]);

			num[i] = num[i] + 1;
			num[j] = num[j] + 1;

			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] <= imd[j][i]) {

				if ((x > avg[i] && y <= avg[j])) {
					if (x > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1 * (1 - beta));
						energy[j] = y - Math.abs(delta1);
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > imd[j][i]) {
				if ((x > avg[i] && y <= avg[j])) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2);
						energy[j] = y + Math.abs(delta2 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;

	}

	public Results protocolIMD_POA_average_multiple(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA average------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				sumPrediction = sumPrediction + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction);

			}

			ArrayList<String> pairs_multiple = util.getInteractingPairs_multiple(currentTime, 1);

			for (int pair = 0; pair < pairs_multiple.size(); pair++) {
				String pairs = pairs_multiple.get(pair);
				if (pairs.compareTo(0 + "," + 0) == 0)
					continue;

				int i = Integer.parseInt(pairs.split(",")[0]);
				int j = Integer.parseInt(pairs.split(",")[1]);

				// Calculate delta
				// avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
				// avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));
				// avg[i] = (avg[i] + avg[j] )/(num[i]+num[j]);
				// avg[j] = (avg[i] + avg[j] )/(num[i]+num[j]);
				avg[i] = (avg[i] * num[i] + avg[j] * num[j]) / (num[i] + num[j]);
				avg[j] = (avg[i] * num[i] + avg[j] * num[j]) / (num[i] + num[j]);

				num[i] = num[i] + 1;
				num[j] = num[j] + 1;

				double delta1 = (energy[i] - avg[i]);

				double delta2 = (energy[j] - avg[j]);
				double x = energy[i];
				double y = energy[j];

				if (imd[i][j] <= imd[j][i]) {

					if ((x > avg[i] && y <= avg[j])) {
						if (x > delta1) {
							energy[i] = x - (delta1);
							energy[j] = y + (delta1 * (1 - beta));
							totalenergyex = totalenergyex + (Math.abs(delta1));
							numInteractions++;

						}
					} else if ((x <= avg[i] && y > avg[j])) {
						if (energy[j] > Math.abs(delta1)) {
							energy[i] = x + Math.abs(delta1 * (1 - beta));
							energy[j] = y - Math.abs(delta1);
							totalenergyex = totalenergyex + (Math.abs(delta1));
							numInteractions++;
						}
					}

				} else if (imd[i][j] > imd[j][i]) {
					if ((x > avg[i] && y <= avg[j])) {
						if (energy[i] > Math.abs(delta2)) {
							energy[i] = x - Math.abs(delta2);
							energy[j] = y + Math.abs(delta2 * (1 - beta));
							totalenergyex = totalenergyex + (Math.abs(delta2));

							numInteractions++;

						}
					} else if ((x <= avg[i] && y > avg[j])) {
						if (energy[j] > Math.abs(delta2)) {
							energy[i] = x + Math.abs(delta2 * (1 - beta));
							energy[j] = y - Math.abs(delta2);
							totalenergyex = totalenergyex + (Math.abs(delta2));
							numInteractions++;
						}
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;

	}

	public Results protocolIMD_POA_updated_sync_info_k(double[][] imd, int K) {
		System.out.println("---STARTING GREEDY WITH IMD POA average------");
		this.K = K;

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		Memory[] memory = new Memory[N + 1];

		// Need to initialize

		for (int i = 0; i < N + 1; i++) {
			memory[i] = new Memory();
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				sumPrediction = sumPrediction + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / (double) (N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / (double) (N + 1));

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Add to memory.

			memory[i].addToMemory(j, energy[j], currentTime, K);
			memory[i].addToMemory(i, energy[i], currentTime, K);

			memory[j].addToMemory(i, energy[i], currentTime, K);
			memory[j].addToMemory(j, energy[j], currentTime, K);

			memory[i].syncMemory(memory[j], i, K);
			// System.out.println("I is " + memory[i].energy_register.size());

			memory[j].syncMemory(memory[i], j, K);
			// System.out.println("J is " + memory[j].energy_register.size());

			avg[i] = memory[i].getAverage();
			avg[j] = memory[j].getAverage();

			// System.out.println("Avg at time " + currentTime + " is " + avg[i]
			// + ", " + avg[j]);

			/*
			 * //Using POA Methods avg[i] = ((avg[i] *
			 * memory[i].energy_register.size()) +
			 * energy[i])/(memory[i].energy_register.size() + 1); avg[j] =
			 * ((avg[j] * memory[j].energy_register.size()) +
			 * energy[j])/(memory[j].energy_register.size() + 1);
			 */

			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);

			if (delta1 > PARAMETERS.energy_threshold) {
				delta1 = delta1 - PARAMETERS.energy_threshold;
			}

			if (delta2 > PARAMETERS.energy_threshold) {
				delta2 = delta2 - PARAMETERS.energy_threshold;
			}
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] < (imd[j][i] + PARAMETERS.threshold)) {

				if ((x > avg[i] && y <= avg[j])) {
					if (x > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1);
						energy[j] = y - Math.abs(delta1) - (beta * Math.abs(delta1));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > imd[j][i] + PARAMETERS.threshold) {
				if ((x > avg[i] && y <= avg[j])) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2);
						energy[j] = y + Math.abs(delta2 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		// System.exit(0);
		return rs;

	}

	public Results protocolIMD_POA_updated_sync_info_k_min_energy(double[][] imd, int K) {
		System.out.println("---STARTING GREEDY WITH IMD POA average------");
		this.K = K;

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		Memory[] memory = new Memory[N + 1];

		// Need to initialize

		for (int i = 0; i < N + 1; i++) {
			memory[i] = new Memory();
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				sumPrediction = sumPrediction + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy / (double) (N + 1));
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction / (double) (N + 1));

			}

			String pairs = util.getInteractingPairs_minEnergy(currentTime, 1, energy, avg);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Add to memory.

			memory[i].addToMemory(j, energy[j], currentTime, K);
			memory[i].addToMemory(i, energy[i], currentTime, K);

			memory[j].addToMemory(i, energy[i], currentTime, K);
			memory[j].addToMemory(j, energy[j], currentTime, K);

			memory[i].syncMemory(memory[j], i, K);
			// System.out.println("I is " + memory[i].energy_register.size());

			memory[j].syncMemory(memory[i], j, K);
			// System.out.println("J is " + memory[j].energy_register.size());

			avg[i] = memory[i].getAverage();
			avg[j] = memory[j].getAverage();

			// System.out.println("Avg at time " + currentTime + " is " + avg[i]
			// + ", " + avg[j]);

			/*
			 * //Using POA Methods avg[i] = ((avg[i] *
			 * memory[i].energy_register.size()) +
			 * energy[i])/(memory[i].energy_register.size() + 1); avg[j] =
			 * ((avg[j] * memory[j].energy_register.size()) +
			 * energy[j])/(memory[j].energy_register.size() + 1);
			 */

			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);

			/*
			 * if(delta1 > PARAMETERS.energy_threshold) { delta1 = delta1 -
			 * PARAMETERS.energy_threshold; }
			 * 
			 * if(delta2 > PARAMETERS.energy_threshold) { delta2 = delta2 -
			 * PARAMETERS.energy_threshold; }
			 */
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] < (imd[j][i])) {

				if ((x > avg[i] && y <= avg[j])) {
					if (x > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1 * (1 - beta));
						energy[j] = y - Math.abs(delta1);
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > imd[j][i]) {
				if ((x > avg[i] && y <= avg[j])) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2);
						energy[j] = y + Math.abs(delta2 * (1 - beta));
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		// System.exit(0);
		return rs;

	}

	public double[] incrementValue(double energy, double[] energies) {
		int i = 0;
		if (energy >= 0 && energy <= 10) {
			i = 0;
		}
		if (energy > 10 && energy <= 20) {
			i = 1;
		}
		if (energy > 20 && energy <= 30) {
			i = 2;
		}
		if (energy > 30 && energy <= 40) {
			i = 3;
		}
		if (energy > 40 && energy <= 50) {
			i = 4;
		}
		if (energy > 50 && energy <= 60) {
			i = 5;
		}
		if (energy > 60 && energy <= 70) {
			i = 6;
		}
		if (energy > 70 && energy <= 80) {
			i = 7;
		}
		if (energy > 80 && energy <= 90) {
			i = 8;
		}
		if (energy > 90 && energy <= 100) {
			i = 9;
		}
		energies[i]++;
		return energies;
	}

	public Results protocolIMD_POA_KnownTarget(double[][] imd, double[] energyLevels) {
		System.out.println("---STARTING GREEDY WITH IMD POA average------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];
		double knownTarget = PARAMETERS.known_target;

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = knownTarget;// energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		ArrayList<Integer> interactedNodes = new ArrayList<>();

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);
			// double target = PARAMETERS.known_target;

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				sumPrediction = sumPrediction + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// if(!interactedNodes.contains(i) && !interactedNodes.contains(j))
			{
				double delta1 = (energy[i] - avg[i]);

				double delta2 = (energy[j] - avg[j]);
				double x = energy[i];
				double y = energy[j];

				if (imd[i][j] <= imd[j][i]) {

					if ((x > avg[i] && y < avg[j])) {
						if (x > delta1) {
							energy[i] = x - (delta1);
							interactedNodes.add(i);
							energy[j] = y + (delta1 * (1 - beta));
							totalenergyex = /* totalenergyex + */ (Math.abs(delta1));

							numInteractions++;
							// System.out.println("Interacting between " + i + "
							// and " + j + " at time " + currentTime + " with
							// energy exchange " + totalenergyex);

						}
					} else if ((x < avg[i] && y > avg[j])) {
						if (energy[j] > Math.abs(delta1)) {
							energy[i] = x + Math.abs(delta1);
							energy[j] = y - Math.abs(delta1) - (delta1 * (1 - beta));
							interactedNodes.add(i);

							totalenergyex = /* totalenergyex + */ (Math.abs(delta1));
							numInteractions++;
							// System.out.println("Interacting between " + i + "
							// and " + j + " at time " + currentTime + " with
							// energy exchange " + totalenergyex);
						}
					}

				} else if (imd[i][j] > imd[j][i]) {
					if ((x > avg[i] && y < avg[j])) {
						if (energy[i] > Math.abs(delta2)) {
							energy[i] = x - Math.abs(delta2) - (1 - beta) * delta1;
							interactedNodes.add(j);

							energy[j] = y + Math.abs(delta2);
							totalenergyex = /* totalenergyex + */ (Math.abs(delta2));

							numInteractions++;
							// System.out.println("Interacting between " + i + "
							// and " + j + " at time " + currentTime + " with
							// energy exchange " + totalenergyex);

						}
					} else if ((x < avg[i] && y > avg[j])) {
						if (energy[j] > Math.abs(delta2)) {
							energy[i] = x + (Math.abs(delta2) - (1 - beta) * delta2);
							energy[j] = y - Math.abs(delta2);
							interactedNodes.add(j);

							totalenergyex = /* totalenergyex + */ (Math.abs(delta2));
							numInteractions++;
							// System.out.println("Interacting between " + i + "
							// and " + j + " at time " + currentTime + " with
							// energy exchange " + totalenergyex);
						}
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		for (int i = 0; i < N + 1; i++) {

			energyLevels = incrementValue(energy[i], energyLevels);
		}

		return rs;

	}

	public Results protocolIMD_POA_KnownTarget_Update(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA average------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];
		double knownTarget = PARAMETERS.known_target;

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = knownTarget;// energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		ArrayList<Integer> interactedNodes = new ArrayList<>();

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 *//*
				 * double target = 0; for(int i=0;i<(N+1) ; i++) { target =
				 * target + energy[i]; } target = target/(N+1);
				 */
			double target = PARAMETERS.known_target;

			double sumPrediction = 0;

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);

				totalenergy = totalenergy + energy[i];
				sumPrediction = sumPrediction + avg[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			if (!interactedNodes.contains(i) && !interactedNodes.contains(j)) {

				// System.out.println("Nodes are " + i + " and " + j);
				double delta1 = Math.abs(energy[i] - avg[i]);

				double delta2 = Math.abs((energy[j] - avg[j]));
				double x = energy[i];
				double y = energy[j];

				if (imd[i][j] < imd[j][i]) {

					// if ((x > avg[i] && y < avg[j]) )

					if (x > avg[i]) {
						if (((1 - beta) * delta1 + y) <= 100) {
							energy[i] = x - delta1;

							// System.out.println("Energy i is " + energy[i]);
							energy[j] = y + ((1 - beta) * delta1);
							/*
							 * if(energy[i] == 45) {
							 * System.out.println(numInteractions + ", " +
							 * "finalized i" + " , " + i); }
							 */
							numInteractions++;
							interactedNodes.add(i);
						}
					}

					else if (x < avg[i]) {
						if ((y - delta1 - (beta * delta1)) >= 0) {
							energy[i] = x + delta1;
							/*
							 * if(energy[i] == 45) {
							 * System.out.println(numInteractions + ", " +
							 * "finalized i" + " , " + i); }
							 */
							// System.out.println("Energy i is " + energy[i]);
							energy[j] = y - delta1 - (beta * delta1);
							numInteractions++;
							interactedNodes.add(i);
						}
					}

				} else if (imd[i][j] > imd[j][i]) {
					// if ((x > avg[i] && y < avg[j]) )

					if (y > avg[j]) {
						if (((1 - beta) * delta2 + x) <= 100) {
							energy[j] = y - delta2;
							// System.out.println("Energy j is " + energy[j]);
							energy[i] = x + ((1 - beta) * delta2);
							/*
							 * if(energy[j] == 45) {
							 * System.out.println(numInteractions + ", " +
							 * "finalized j " + " , " + j); }
							 */
							numInteractions++;
							interactedNodes.add(j);
						}
					}

					else if (y < avg[j]) {
						if ((x - delta2 - (beta * delta2)) >= 0) {

							energy[j] = y + delta2;
							// System.out.println("Energy j is " + energy[j]);
							energy[i] = x - delta2 - (beta * delta2);
							numInteractions++;
							/*
							 * if(energy[j] == 45) {
							 * System.out.println(numInteractions + ", " +
							 * "finalized j" + " , " + j); }
							 */
							interactedNodes.add(j);
						}
					}
				}

			} else {
				/*
				 * if(interactedNodes.contains(i)) { System.out.println(
				 * "Repeated Nodes " + i ); } if(interactedNodes.contains(j)) {
				 * System.out.println("Repeated Nodes " + j ); }
				 */
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		/*
		 * for(int i=0; i<N+1; i++) {
		 * 
		 * System.out.println(i + " , " + energy[i]); }
		 */
		return rs;

	}

	public Results protocolIMD_POA_LossAware(double[][] imd) {
		System.out.println("---STARTING GREEDY WITH IMD POA LossAware------");

		double[] num = new double[N + 1];
		double avg[] = new double[N + 1];

		// init num and avg
		for (int i = 0; i < N + 1; i++) {
			num[i] = 1;
			avg[i] = energy[i];
		}

		double sum = 0;
		int currentTime = 0;
		int numInteractions = 0;
		double totalenergyex = 0;

		while (true) {

			currentTime++;
			sum = 0;

			double totalenergy = 0;
			int numNodesPlus = 0;
			int numNodesMinus = 0;
			// double target = totalEnergy / (N + 1); //Target is initial mean

			/**
			 * set target as continuous mean.
			 */
			double target = 0;
			double sumPrediction = 0;
			for (int i = 0; i < (N + 1); i++) {
				target = target + energy[i];
			}
			target = target / (N + 1);

			for (int i = 0; i < N + 1; i++) {

				sum = sum + Math.abs(energy[i] - target);
				sumPrediction = sumPrediction + avg[i];
				totalenergy = totalenergy + energy[i];

				if (energy[i] > target) {
					numNodesPlus++;
				} else if (energy[i] < target) {
					numNodesMinus++;
				}

			}
			sum = sum / (N + 1);

			if ((currentTime - 1) % numRecords == 0) {

				rs.setTotalEnergy(totalenergy);
				rs.setvD(sum);
				rs.setNumInteractions(numInteractions);
				rs.setNumNodesPlus(numNodesPlus);
				rs.setNumNodesminus(numNodesMinus);
				rs.setEnergyPrediction(sumPrediction);

			}

			String pairs = util.getInteractingPairs(currentTime, 1);
			if (pairs.compareTo(0 + "," + 0) == 0)
				continue;

			int i = Integer.parseInt(pairs.split(",")[0]);
			int j = Integer.parseInt(pairs.split(",")[1]);

			// Calculate delta
			avg[i] = ((avg[i] * num[i] + energy[j]) / (num[i] + 1));
			avg[j] = ((avg[j] * num[j] + energy[i]) / (num[j] + 1));

			num[i] = num[i] + 1;
			num[j] = num[j] + 1;

			double delta1 = (energy[i] - avg[i]);

			double delta2 = (energy[j] - avg[j]);
			double x = energy[i];
			double y = energy[j];

			if (imd[i][j] <= imd[j][i]) {

				if ((x > avg[i] && y <= avg[j])) {
					if (x > delta1) {
						energy[i] = x - (delta1);
						energy[j] = y + (delta1 * (1 - beta));

						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta1)) {
						energy[i] = x + Math.abs(delta1);
						energy[j] = y - Math.abs(delta1) - (beta * Math.abs(delta1));
						totalenergyex = totalenergyex + (Math.abs(delta1));
						numInteractions++;
					}
				}

			} else if (imd[i][j] > imd[j][i]) {
				if ((x > avg[i] && y <= avg[j])) {
					if (energy[i] > Math.abs(delta2)) {
						energy[i] = x - Math.abs(delta2) - Math.abs(beta * delta2);
						energy[j] = y + Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));

						numInteractions++;

					}
				} else if ((x <= avg[i] && y > avg[j])) {
					if (energy[j] > Math.abs(delta2)) {
						energy[i] = x + Math.abs(delta2 * (1 - beta));
						energy[j] = y - Math.abs(delta2);
						totalenergyex = totalenergyex + (Math.abs(delta2));
						numInteractions++;
					}
				}
			}

			if (currentTime > (timeOfSimulation)) {

				break;
			}

		}

		return rs;

	}

}
