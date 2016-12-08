import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

	public static class Temp {
		private String name;

		public Temp(String n){
			name = n;
		}

		public String getStartingCell(){
			return name + "i";
		}

		public String getEndingCell(){
			return name + "f";
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;

			if(!(obj instanceof Temp)) return false;

			Temp that = (Temp) obj;
			if(this.name.equals(that.name)) return true;

			return false;
		}
	}

	public static Set<Set<String>> powerSet(Set<String> originalSet) {
		Set<Set<String>> sets = new HashSet<Set<String>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<String>());
			return sets;
		}
		List<String> list = new ArrayList<String>(originalSet);
		String head = list.get(0);
		Set<String> rest = new HashSet<String>(list.subList(1, list.size()));
		for (Set<String> set : powerSet(rest)) {
			Set<String> newSet = new HashSet<String>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	public static ArrayList<ArrayList<String>> powerSetArrayList(ArrayList<String> originalArrayList) {
		ArrayList<ArrayList<String>> arrayLists = new ArrayList<ArrayList<String>>();

		if (originalArrayList.isEmpty()) {
			arrayLists.add(new ArrayList<String>());
			return arrayLists;
		}

		List<String> list = new ArrayList<String>(originalArrayList);
		String head = list.get(0);

		ArrayList<String> rest = new ArrayList<String>(list.subList(1, list.size()));
		for (ArrayList<String> set : powerSetArrayList(rest)) {
			ArrayList<String> newArrayList = new ArrayList<String>();
			newArrayList.add(head);
			newArrayList.addAll(set);

			arrayLists.add(newArrayList);
			arrayLists.add(set);
		}

		return arrayLists;
	}

	public static void main(String[] args) {
		ArrayList<Temp> temps = new ArrayList<>();
		temps.add(new Temp("A"));
		temps.add(new Temp("B"));

		/*
		Set<String> setTemps = new HashSet<>();
		setTemps.add("P1");
		setTemps.add("P2");
		setTemps.add("P3");
		setTemps.add("P3");
		Set<Set<String>> result = powerSet(setTemps);
		for(Set<String> s : result){
			if(s.size() == 2){
				for(String ss : s){
					System.out.println(ss);
				}
				System.out.println("-");
			}
		}*/

		ArrayList<String> s = new ArrayList<>();
		s.add("A");
		s.add("B");
		s.add("C");
		ArrayList<ArrayList<String>> ss = powerSetArrayList(s);
		for(ArrayList<String> arr : ss){
			for(String str : arr){
				System.out.println(str);
			}
			System.out.println("-");
		}
	}
}