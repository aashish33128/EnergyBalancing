import java.util.ArrayList;

public class Results {
	
	ArrayList<Double> vD = null;
	ArrayList<Double> energyPrediction = null;
	ArrayList<Double> numNodesPrediction = null;
	ArrayList<Double> totalEnergy = null;
	ArrayList<Double> numInteractions = null;
	ArrayList<Double> numNodesPlus = null; //Number of nodes on the positive side
	ArrayList<Double> numNodesminus = null; //Number of nodes on the negative side.
	
	
	Utilities util = new Utilities();
	
	


	
	int time = PARAMETERS.timeOfSimulation;
	int N = PARAMETERS.numNodes;
	int simTime = PARAMETERS.simTime;
	
	
	
	
	public  Results(int initialize)
	{
		// ArrayList<Double> totalEnergy_temp_poa = new ArrayList<>();
				vD = new ArrayList<>();
				util.init_DArray(vD, (time/ simTime) + 1);
				
				totalEnergy = new ArrayList<>();
				util.init_DArray(totalEnergy, (time/simTime) + 1);
				
				numInteractions = new ArrayList<>();
				util.init_DArray(numInteractions, (time/simTime) + 1);
				
				numNodesPlus = new ArrayList<>();
				util.init_DArray(numNodesPlus, (time/simTime) + 1);
				
				numNodesminus = new ArrayList<>();
				util.init_DArray(numNodesminus, (time/simTime) + 1);
				
				
				energyPrediction = new ArrayList<>();
				util.init_DArray(energyPrediction, (time/simTime) + 1);
				
				
				numNodesPrediction = new ArrayList<>();
				util.init_DArray(numNodesPrediction, (N + 1));
				
	}

	public Results()
	{
		vD = new ArrayList<>();
		
		
		totalEnergy = new ArrayList<>();
		
		
		numInteractions = new ArrayList<>();
		
		
		numNodesPlus = new ArrayList<>();
		
		
		numNodesminus = new ArrayList<>();
		
		
		
		energyPrediction = new ArrayList<>();
		
		
		
		numNodesPrediction = new ArrayList<>();
		
		
	}

	public ArrayList<Double> getvD() {
		return vD;
	}


	public void setvD(double value) {
		this.vD.add(value);
	}


	public ArrayList<Double> getEnergyPrediction() {
		return energyPrediction;
	}


	public void setEnergyPrediction(double energyPrediction) {
		this.energyPrediction.add(energyPrediction);
	}


	public ArrayList<Double> getNumNodesPrediction() {
		return numNodesPrediction;
	}


	public void setNumNodesPrediction(double numNodesPrediction) {
		this.numNodesPrediction.add(numNodesPrediction);
	}


	public ArrayList<Double> getTotalEnergy() {
		return totalEnergy;
	}


	public void setTotalEnergy(double totalEnergy) {
		this.totalEnergy.add(totalEnergy);
	}


	public ArrayList<Double> getNumInteractions() {
		return numInteractions;
	}


	public void setNumInteractions(double numInteractions) {
		this.numInteractions .add(numInteractions);
	}
	
	
	public ArrayList<Double> getNumNodesPlus() {
		return numNodesPlus;
	}


	public void setNumNodesPlus(double numNodesPlus) {
		this.numNodesPlus.add(numNodesPlus);
	}


	public ArrayList<Double> getNumNodesminus() {
		return numNodesminus;
	}


	public void setNumNodesminus(double numNodesminus) {
		this.numNodesminus.add(numNodesminus);
	}
	

}
