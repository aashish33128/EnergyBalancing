import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

public class Memory {

	double K = 0;

	LinkedHashMap<Integer, String> energy_register = new LinkedHashMap<>();
	int numNodes = 0;

	// int[] updatedTime = new int[PARAMETERS.numNodes + 1];

	public void addToMemory(int node, double energy, int currentTime, int K) {
		String value = currentTime + "_" + energy;

		
		if (!energy_register.containsKey(node)) {
			energy_register.put(node, value);
			numNodes++;
		} else {
			String current = energy_register.get(node);
			double time = Double.parseDouble(current.split("_")[0]);
			if(currentTime > time)
			{
				energy_register.put(node, value);
			}
		}
		
		checkMemoryLimit(K);
	}
	
	
	public void removeFromMemory(int node)
	{
		this.energy_register.remove(node);
	}
	
	public void checkMemoryLimit(int K)
	{
		if(this.energy_register.size() > K)
		{
			//System.out.println("Before removing " + this.energy_register.size());
			
			
			int id = this.energy_register.entrySet().iterator().next().getKey();
			this.energy_register.remove(id);
			
			
			//System.out.println("Removed " + this.energy_register.size());
		}
	}
	
	public double getAverage()
	{
		Iterator<Entry<Integer, String>> iter = this.energy_register.entrySet().iterator();
		double sum = 0;
		while(iter.hasNext())
		{
			int node = iter.next().getKey();
		
			String value = this.energy_register.get(node);
			double energy = Double.parseDouble(value.split("_")[1]);
			sum = sum + energy;
		}
		
		double average = sum/this.energy_register.size();
		return average;
		
	}
	
	
	public void syncMemory(Memory m1, int ownid, int K)
	{
		LinkedHashMap<Integer, String> mem = m1.energy_register;
		Iterator<Entry<Integer, String>> iter = mem.entrySet().iterator();
		while(iter.hasNext())
		{
			int nodetoadd = iter.next().getKey();
			double energy = Double.parseDouble(mem.get(nodetoadd).split("_")[1]);
			int currentTime = Integer.parseInt(mem.get(nodetoadd).split("_")[0]);
			
			if(nodetoadd!=ownid)
			this.addToMemory(nodetoadd, energy, currentTime, K);
		}
		
	}
	
	
	
	
	
	

}
