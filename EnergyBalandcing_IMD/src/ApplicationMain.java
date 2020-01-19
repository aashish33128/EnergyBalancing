import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ApplicationMain {

	int N = PARAMETERS.numNodes;
	int simulationTime = PARAMETERS.timeOfSimulation;
	Utilities util = new Utilities();
	int simTime = PARAMETERS.simTime;
	
	
	public double[][] getIMDInfo(int[][][] meetingInfo, int type, double[][] Mij) throws IOException {
		double[][] imd = new double[N + 1][N + 1];
		
		for (int n1 = 0; n1 < (N + 1); n1++) {
			for (int n2 = 0; n2 < (N + 1); n2++) {
				if (n1 != n2) {
					int[] time1 = meetingInfo[n1][n2];
					/**
					 * See how many times the meeting between n1 and n2 is
					 * counted; each time i == n2 , a next set of imd is
					 * computed: countSteps
					 */
					if (type == 0) {
						imd[n1][n2] = util.getIntermeetingDegree(n1, n2, time1, meetingInfo);
					} else if (type == 1) {
						imd[n1][n2] = util.caluclateIMDUsingLamda(n1, n2, Mij);
					}
				
					//System.out.println("Intermeeting Degrees for " + n1 + " ->" + n2 + " is " + imd[n1][n2]);
					//System.out.println("Intermeeting Degrees for " + n2 + " ->" + n1 + " is " + imd[n2][n1]);
					

				}
			}
		}

		return imd;

		// int[] time2 = meetingInfo[n2][]
	}


	public static void main(String[] args) throws IOException {

		ApplicationMain main = new ApplicationMain();
		Utilities util = main.util;

		double[] energy = new double[main.N + 1];

		// int numOfMeetingsToGenerate = 100;
		ArrayList<Results> results_poa_probabilistic = new ArrayList<>();
		ArrayList<Results> results_poa_Mij = new ArrayList<>();
		ArrayList<Results> results_poa_global = new ArrayList<>();
		ArrayList<Results> results_greedy_positive= new ArrayList<>();
		ArrayList<Results> results_greedy_negative = new ArrayList<>();
		
		ArrayList<Results> results_imd = new ArrayList<>();
		ArrayList<Results> results_imdcasewise = new ArrayList<>();
		ArrayList<Results> results_imdthreshold = new ArrayList<>();
		ArrayList<Results> results_imd_poa = new ArrayList<>();
		
		
		
		
		
		

		int simTime = main.simTime;

		double[] energyPred = new double[main.N + 1];
		double[] numPred = new double[main.N + 1];
		double totalenergy = 0;

		for (int i = 0; i < (main.N + 1); i++) {

			energyPred[i] = 0;
			numPred[i] = 0;
			totalenergy = totalenergy + energy[i];
		}

		// Run Simulation
		int[][][] meetingInfo = null;
		double[] energyLevels = new double[10];
		
		for(int x =0;x<10;x++)
		{
			energyLevels[x] = 0;
		}
	//	util.init_DArray(energyLevels, energyLevels.length);
		for (int sim = 0; sim < simTime; sim++) {

			util.setEnergyLevels(energy);

			double[] energyInit = new double[main.N + 1];
			energyInit = energy.clone();
			
			
			energy = energyInit.clone();
			Results result1 = new Results();
			
			
			double[][] imd = new double[main.N + 1][main.N + 1];
			double[][] Mij = new double[main.N + 1][main.N + 1];
			meetingInfo = util.setNetworkInfo(energy, Mij);
			imd = main.getIMDInfo(meetingInfo, 1, Mij);
			
			energy = energyInit.clone();
			Results result2 = new Results();
			Protocols protocol2 = new Protocols(energy, meetingInfo, totalenergy, result2);
			result2 = protocol2.run_POA_Mij(energyLevels);
			results_poa_Mij.add(result2);
		
			/*Results result4 = new Results();
			Protocols protocol4 = new Protocols(energy, meetingInfo, totalenergy, result4);
			result4 = protocol4.protocolIMD_POA_KnownTarget(imd);
			results_greedy_positive.add(result4);*/
			
			
			energy = energyInit.clone();
			Results result5 = new Results();
			Protocols protocol5 = new Protocols(energy, meetingInfo, totalenergy, result5);
			result5 = protocol5.run_PGT_global_self_POA();
			results_greedy_positive.add(result5);
			
			/*energy = energyInit.clone();
			Results result6 = new Results();
			Protocols protocol6 = new Protocols(energy, meetingInfo, totalenergy, result6);
			result6 = protocol6.protocolIMD_POA_updated_sync_info_k(imd, PARAMETERS.numNodes);
			results_imd.add(result6);*/
			
			energy = energyInit.clone();
			Results result7 = new Results();
			Protocols protocol7 = new Protocols(energy, meetingInfo, totalenergy, result7);
			result7 = protocol7.protocolIMD_POA_restricted(imd);
			results_imdcasewise.add(result7);
			
			
			energy = energyInit.clone();
			Results result8 = new Results();
			Protocols protocol8 = new Protocols(energy, meetingInfo, totalenergy, result8);
			result8 = protocol8.protocolIMD_POA_restricted_K_window(imd, 4);
			results_imd_poa.add(result8);
			
			energy = energyInit.clone();
		
			/*double[] energies = new double[main.N+1];
			Results result9 = new Results();
			Protocols protocol9 = new Protocols(energy, meetingInfo, totalenergy, result9);
			result9 = protocol9.protocolIMD_WeightedPOA(imd);//(imd, energyLevels);
			
			
			for(int i=0; i<result9.vD.size();i++)
			{
				System.out.println("--------------------" + result9.vD.get(i));
			}
			results_imd_poa.add(result9);*/
			
		
			

		}
		
		
		for(int a =0; a<10;a++)
		{
			//energyLevels[a] = energyLevels[a]/simTime;
			System.out.println(energyLevels[a]);
		}

		/*Results finalResult_poa_probabilistic = new Results(1);
		finalResult_poa_probabilistic = main.calculateFinalResult(results_poa_probabilistic);*/
		
		Results finalResult_poa_mij = new Results(1);
		finalResult_poa_mij = main.calculateFinalResult(results_poa_Mij);
		
	/*	Results finalResult_poa_global = new Results(1);
		finalResult_poa_global = main.calculateFinalResult(results_poa_global);*/
		
		Results finalResult_greedy_positive = new Results(1);
		finalResult_greedy_positive = main.calculateFinalResult(results_greedy_positive);
		
		/*Results finalResult_greedy_negative = new Results(1);
		finalResult_greedy_negative = main.calculateFinalResult(results_greedy_negative);
		
		Results finalResult_imd = new Results(1);
		finalResult_imd = main.calculateFinalResult(results_imd); */
		
	/*	Results finalResult_imd_threshold = new Results(1);
		finalResult_imd_threshold = main.calculateFinalResult(results_imdthreshold);*/
		
		Results finalResult_imd_casewise = new Results(1);
		finalResult_imd_casewise = main.calculateFinalResult(results_imdcasewise);
		//System.out.println(finalResult.vD.size());
	
		Results finalResult_imd_poa = new Results(1);
		finalResult_imd_poa = main.calculateFinalResult(results_imd_poa);
			
		
		

		//util.writeToFile(finalResult_poa_probabilistic, true, "poa_probabilistic"); //boolean flag to print data on console
		util.writeToFile(finalResult_poa_mij, true, "poa_Mij"); //boolean flag to print data on console
	//	util.writeToFile(finalResult_poa_global, true, "poa_average"); //boolean flag to print data on console
		util.writeToFile(finalResult_greedy_positive, true, "poa_greedy_positive"); //boolean flag to print data on console
		//util.writeToFile(finalResult_greedy_negative, true, "imd_poa"); //boolean flag to print data on console
		//util.writeToFile(finalResult_imd, true, "imd_sync_N"); //boolean flag to print data on console
		util.writeToFile(finalResult_imd_casewise, true, "imd_poa"); //boolean flag to print data on console
		util.writeToFile(finalResult_imd_poa, true, "imd_sync_5"); //boolean flag to print data on console
		//util.writeToFile(finalResult_imd_poa, true, "imd_proportional"); //boolean flag to print data on console
		
		
	}

	/**
	 * Final Result : Sum and take the average
	 * 
	 * @param results
	 * @return
	 */
	public Results calculateFinalResult(ArrayList<Results> results) {
		Results r = new Results();

		/**
		 * Initialize and Define
		 */
		ArrayList<Double> vD = new ArrayList<>();
		util.init_DArray(vD, results.get(0).vD.size());
		ArrayList<Double> energyPrediction = new ArrayList<>();
		util.init_DArray(energyPrediction, results.get(0).energyPrediction.size());
		ArrayList<Double> numNodesPrediction = new ArrayList<>();
		util.init_DArray(numNodesPrediction, results.get(0).numNodesPrediction.size());
		ArrayList<Double> totalEnergy = new ArrayList<>();
		util.init_DArray(totalEnergy, results.get(0).totalEnergy.size());
		ArrayList<Double> numInteractions = new ArrayList<>();
		util.init_DArray(numInteractions, results.get(0).numInteractions.size());
		ArrayList<Double> numNodesPlus = new ArrayList<>();
		util.init_DArray(numNodesPlus, results.get(0).numNodesPlus.size());
		ArrayList<Double> numNodesminus = new ArrayList<>();
		util.init_DArray(numNodesminus, results.get(0).numNodesminus.size());

		/**
		 * Add each result
		 */
		
	
		for (int i = 0; i < results.size(); i++) {

			
			for (int c = 0; c < results.get(i).vD.size(); c++) {
				
			
				vD.set(c, (results.get(i).vD.get(c) + vD.get(c)));
				
			}

			for (int b = 0; b < results.get(i).energyPrediction.size(); b++) {
				energyPrediction.set(b, (results.get(i).energyPrediction.get(b) + energyPrediction.get(b)));
			}

			for (int c = 0; c < results.get(i).numNodesPrediction.size(); c++) {
				numNodesPrediction.set(c, (results.get(i).numNodesPrediction.get(c) + numNodesPrediction.get(c)));
			}

			for (int c = 0; c < results.get(i).totalEnergy.size(); c++) {
				totalEnergy.set(c, (results.get(i).totalEnergy.get(c) + totalEnergy.get(c)));
			}

			for (int c = 0; c < results.get(i).numInteractions.size(); c++) {
				numInteractions.set(c, (results.get(i).numInteractions.get(c) + numInteractions.get(c)));
			}

			for (int c = 0; c < results.get(i).numNodesPlus.size(); c++) {
				numNodesPlus.set(c, (results.get(i).numNodesPlus.get(c) + numNodesPlus.get(c)));
			}

			for (int c = 0; c < results.get(i).numNodesminus.size(); c++) {
				numNodesminus.set(c, (results.get(i).numNodesminus.get(c) + numNodesminus.get(c)));
			}

		}
		
		
		//Create Result Object
		
		r.vD = vD;
		
		//System.out.println(r.vD.get(0));
		r.energyPrediction = energyPrediction;
		r.numNodesPrediction = numNodesPrediction;
		r.totalEnergy = totalEnergy;		
		r.numInteractions = numInteractions;
		r.numNodesPlus = numNodesPlus;
		r.numNodesminus = numNodesminus;

		return r;
	}

}
