package arm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

public class Arm {
    
    public static List<List<Integer>> generateCombinationSets(List<Integer> elements, int r, List<Integer> currentSet, int startIndex) {
        List<List<Integer>> combinations = new ArrayList<>();
        if (currentSet.size()==r) {
            combinations.add(new ArrayList<>(currentSet));
            return combinations;
        }
        
        for (int i=startIndex; i < elements.size(); i++) {
            currentSet.add(elements.get(i));
            combinations.addAll(generateCombinationSets(elements, r, currentSet, i+1));
            currentSet.remove(currentSet.size()-1);
        }
        
        return combinations;
    }
    
    public static List<List<Integer>> generatePermutationSets(List<Integer> elements, int r, List<Integer> currentSet) {
        List<List<Integer>> permutations = new ArrayList<>();

        if (currentSet.size() == r) {
            permutations.add(new ArrayList<>(currentSet));
            return permutations;
        }

        for (Integer element : elements) {
            if (!currentSet.contains(element)) {
                List<Integer> newSet = new ArrayList<>(currentSet);
                newSet.add(element);
                permutations.addAll(generatePermutationSets(elements, r, newSet));
            }
        }

        return permutations;
    }
    
    public static List<List<String>> generateRules(List<List<String>> data, double min_support, double min_confidence){
        Map<Integer, Double> eachItemSupport = new HashMap();
        List<Integer> pruned_data = new ArrayList();
        for (int itemset=0; itemset<=(data.get(0).size()-1); itemset++){
            int freq = 0;
            double support;
            for (int transaction=1; transaction<=(data.size()-1); transaction++){
                if (data.get(transaction).get(itemset).equals("t")){
                    freq+=1;
                }
            }
            support = (double)freq/(data.size()-1);
            if (support >= min_support){
                eachItemSupport.put(itemset, support);
                pruned_data.add(itemset);
            }
        }
        System.out.println(pruned_data.size());
        
        int r=2;
        List<List<Integer>> initial_combinations = generateCombinationSets(pruned_data, r, new ArrayList(), 0);
        
        List<List<Integer>> final_combinations = new ArrayList();
        Map<Set<Integer>, Double> eachCombinationSupport = new HashMap();
        List<List<Integer>> pruning = new ArrayList();
        
        for (List<Integer> combination : initial_combinations){
            int freq = 0;
            double support;
            for (int transaction=1; transaction<=data.size()-1; transaction++){
                boolean flag = false;
                for (int itemset=0; itemset<=combination.size()-1; itemset++){
                    if (data.get(transaction).get(combination.get(itemset)).equals("t")){
                        flag = true;
                    }
                    else{
                        flag = false;
                        break;
                    }
                }
                if (flag==true){
                    freq+=1;
                }
            }
            support = ((double)freq/(data.size()-1));
            if (support >= min_support){
                eachCombinationSupport.put(new HashSet(initial_combinations.get(initial_combinations.indexOf(combination))), support);
                final_combinations.add(initial_combinations.get(initial_combinations.indexOf(combination)));
                pruning.add(initial_combinations.get(initial_combinations.indexOf(combination)));
            }
        }
        System.out.println(pruning.size());

        int sample_size = 3;
        while(pruning.size()>0){
            List<List<Integer>> combinations = new ArrayList();
            for (List<Integer> combination: pruning){
                for (int combination_index=0; combination_index<=pruning.size()-1; combination_index++){
                    for (int itemset_index=0; itemset_index<=combination.size()-1; itemset_index++){
                        Set<Integer> unique_element = new HashSet(combination);
                        unique_element.add(pruning.get(combination_index).get(itemset_index));
                        if (unique_element.size()==sample_size){
                            boolean flag = true;
                            for(List<Integer> combination_item: combinations){
                                if(combination_item.containsAll(new ArrayList(unique_element))){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag==true){
                                List<Integer> new_combination = new ArrayList(unique_element);
                                int freq = 0;
                                double support;
                                for (int transaction=1; transaction<=data.size()-1; transaction++){
                                    boolean freq_flag = false;
                                    for (int itemset=0; itemset<=new_combination.size()-1; itemset++){
                                        if (data.get(transaction).get(new_combination.get(itemset)).equals("t")){
                                            freq_flag = true;
                                        }
                                        else{
                                            freq_flag = false;
                                            break;
                                        }
                                    }
                                    if (freq_flag==true){
                                        freq+=1;
                                    }
                                }
                                support = (double)freq/(data.size()-1);
                                if (support >= min_support){
                                    eachCombinationSupport.put(new HashSet(new_combination), support);
                                    final_combinations.add(new_combination);
                                    combinations.add(new_combination);
                                }
                            }
                        }
                    }
                }
            }
            pruning.clear();
            pruning.addAll(combinations);
            sample_size++;
            System.out.println(pruning.size());

        }
        
        List<List<Integer>> rules = new ArrayList();
        Map<List<Integer>, Double> rulesConfidence = new HashMap();
        for (List<Integer> combination: final_combinations){
            List<List<Integer>> initial_permutations = generatePermutationSets(combination, combination.size(), new ArrayList());
            for (List<Integer> permutation: initial_permutations){
                double confidence;
                if (permutation.size()==2){
                    confidence = eachCombinationSupport.get(new HashSet(permutation))/eachItemSupport.get(permutation.get(0));
                    if (confidence > min_confidence){
                        rules.add(permutation);
                        rulesConfidence.put(permutation, confidence);
                    }
                }
                else{
                    confidence = eachCombinationSupport.get(new HashSet(permutation))/eachCombinationSupport.get(new HashSet(permutation.subList(0, permutation.size()-1)));
                    if (confidence > min_confidence){
                        rules.add(permutation);
                        rulesConfidence.put(permutation, confidence);
                    }
                }
                
            }
        }
        
        List<List<String>> final_rules = new ArrayList();
        for (List<Integer> rule: rules){
            List<String> flag = new ArrayList();
            for (int index=0; index<=rule.size()-1; index++){
                flag.add(data.get(0).get(rule.get(index)));
            }
            final_rules.add(flag);
        }
        
        return final_rules;
    }

    
public static Map<Set<Integer>, List<Integer>> getFrequentTransactions(List<List<String>> data, double min_support){
        //Map<Integer, Double> eachItemSupport = new HashMap();
        Map<Set<Integer>, List<Integer>> transactionsOfCombinations = new HashMap();
        List<Integer> pruned_data = new ArrayList();
        for (int itemset=0; itemset<=(data.get(0).size()-1); itemset++){
            int freq = 0;
            double support;
            for (int transaction=1; transaction<=(data.size()-1); transaction++){
                if (data.get(transaction).get(itemset).equals("t")){
                    freq+=1;
                }
            }
            support = (double)freq/(data.size()-1);
            if (support >= min_support){
                //eachItemSupport.put(itemset, support);
                pruned_data.add(itemset);
            }
        }
        System.out.println(pruned_data.size());
        
        int r=2;
        List<List<Integer>> initial_combinations = generateCombinationSets(pruned_data, r, new ArrayList(), 0);
        
        //Map<Set<Integer>, Double> eachCombinationSupport = new HashMap();
        List<List<Integer>> pruning = new ArrayList();
        
        for (List<Integer> combination : initial_combinations){
            int freq = 0;
            double support;
            List<Integer> transaction_indices = new ArrayList();
            for (int transaction=1; transaction<=data.size()-1; transaction++){
                boolean flag = false;
                for (int itemset=0; itemset<=combination.size()-1; itemset++){
                    if (data.get(transaction).get(combination.get(itemset)).equals("t")){
                        flag = true;
                    }
                    else{
                        flag = false;
                        break;
                    }
                }
                if (flag==true){
                    freq+=1;
                    transaction_indices.add(transaction);
                }
            }
            support = ((double)freq/(data.size()-1));
            if (support >= min_support){
                //eachCombinationSupport.put(new HashSet(initial_combinations.get(initial_combinations.indexOf(combination))), support);
                pruning.add(initial_combinations.get(initial_combinations.indexOf(combination)));
                transactionsOfCombinations.put(new HashSet(initial_combinations.get(initial_combinations.indexOf(combination))), transaction_indices);
                /*for (int transaction=1; transaction<=data.size()-1; transaction++){
                    boolean flag = false;
                    for (int itemset=0; itemset<=combination.size()-1; itemset++){
                        if (data.get(transaction).get(combination.get(itemset)).equals("t")){
                            flag = true;
                        }
                        else{
                            flag = false;
                            break;
                        }
                    }
                    if (flag==true){
                        rule_based_data.add(transaction);
                    }
                }*/
                //final_combinations.add(initial_combinations.get(initial_combinations.indexOf(combination)));
            }
        }
        System.out.println(pruning.size());

        int sample_size = 3;
        while(pruning.size()>0){
            List<List<Integer>> combinations = new ArrayList();
            for (List<Integer> combination: pruning){
                for (int combination_index=0; combination_index<=pruning.size()-1; combination_index++){
                    for (int itemset_index=0; itemset_index<=combination.size()-1; itemset_index++){
                        Set<Integer> unique_element = new HashSet(combination);
                        unique_element.add(pruning.get(combination_index).get(itemset_index));
                        if (unique_element.size()==sample_size){
                            boolean flag = true;
                            for(List<Integer> combination_item: combinations){
                                if(combination_item.containsAll(new ArrayList(unique_element))){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag==true){
                                List<Integer> new_combination = new ArrayList(unique_element);
                                int freq = 0;
                                double support;
                                List<Integer> transaction_indices = new ArrayList();
                                for (int transaction=1; transaction<=data.size()-1; transaction++){
                                    boolean freq_flag = false;
                                    for (int itemset=0; itemset<=new_combination.size()-1; itemset++){
                                        if (data.get(transaction).get(new_combination.get(itemset)).equals("t")){
                                            freq_flag = true;
                                        }
                                        else{
                                            freq_flag = false;
                                            break;
                                        }
                                    }
                                    if (freq_flag==true){
                                        freq+=1;
                                        transaction_indices.add(transaction);
                                    }
                                }
                                support = (double)freq/(data.size()-1);
                                if (support >= min_support){
                                    combinations.add(new_combination);
                                    transactionsOfCombinations.put(new HashSet(new_combination), transaction_indices);
                                    /*for (int transaction=1; transaction<=data.size()-1; transaction++){
                                        boolean freq_flag = false;
                                        for (int itemset=0; itemset<=combination.size()-1; itemset++){
                                            if (data.get(transaction).get(combination.get(itemset)).equals("t")){
                                                freq_flag = true;
                                            }
                                            else{
                                                freq_flag = false;
                                                break;
                                            }
                                        }
                                        if (freq_flag==true){
                                            rule_based_data.add(transaction);
                                        }
                                    }*/
                                    //eachCombinationSupport.put(new HashSet(new_combination), support);
                                    //final_combinations.add(new_combination);
                                    //combinations.add(new_combination);
                                }
                            }
                        }
                    }
                }
            }
            pruning.clear();
            pruning.addAll(combinations);
            sample_size++;
            System.out.println(pruning.size());

        }
        
        return transactionsOfCombinations;
    }
    
    public static void main(String[] args) throws IOException {
         
        // Reading CSV file
        String filepath = "F:/FYP/ARM in Java/supermarket.csv";
        String line = "";
        String delimeter = ",";
        List<List<String>> data = new ArrayList();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            while ((line = br.readLine()) != null){
                String[] attributes = line.split(delimeter);
                List<String> record = new ArrayList();
                for (String attribute : attributes){
                    record.add(attribute);
                }
                data.add(record);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        double min_support = 0.3;
        double min_confidence = 0.7;
        
        //List<List<String>> rules = generateRules(data, min_support, min_confidence);
        
        /*for (List<String> rule: rules){
            System.out.println(rule.subList(0, rule.size()-1).toString() + " ==> " + rule.get(rule.size()-1));
        }*/
        
        Map<Set<Integer>, List<Integer>> indices_of_frequent_transactions = getFrequentTransactions(data, min_support);
        
        for(Map.Entry<Set<Integer>, List<Integer>> eachCombinationTransactions : indices_of_frequent_transactions.entrySet()){
            System.out.println(eachCombinationTransactions.getKey().toString() + " : " + eachCombinationTransactions.getValue().toString());
    }
        
    }
    
}