package util;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Permutations<E> implements  Iterator<E[]>{

    private E[] arr;
    private int[] ind;
    private boolean has_next;

    public E[] output;//next() returns this array, make it public

    public Permutations(E[] arr){
        this.arr = arr.clone();
        ind = new int[arr.length];
        //convert an array of any elements into array of integers - first occurrence is used to enumerate
        Map<E, Integer> hm = new HashMap<E, Integer>();
        for(int i = 0; i < arr.length; i++){
            Integer n = hm.get(arr[i]);
            if (n == null){
                hm.put(arr[i], i);
                n = i;
            }
            ind[i] = n.intValue();
        }
        Arrays.sort(ind);//start with ascending sequence of integers


        //output = new E[arr.length]; <-- cannot do in Java with generics, so use reflection
        output = (E[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
        has_next = true;
    }

    public boolean hasNext() {
        return has_next;
    }

    /**
     * Computes next permutations. Same array instance is returned every time!
     * @return
     */
    public E[] next() {
        if (!has_next)
            throw new NoSuchElementException();

        for(int i = 0; i < ind.length; i++){
            output[i] = arr[ind[i]];
        }


        //get next permutation
        has_next = false;
        for(int tail = ind.length - 1;tail > 0;tail--){
            if (ind[tail - 1] < ind[tail]){//still increasing

                //find last element which does not exceed ind[tail-1]
                int s = ind.length - 1;
                while(ind[tail-1] >= ind[s])
                    s--;

                swap(ind, tail-1, s);

                //reverse order of elements in the tail
                for(int i = tail, j = ind.length - 1; i < j; i++, j--){
                    swap(ind, i, j);
                }
                has_next = true;
                break;
            }

        }
        return output;
    }

    private void swap(int[] arr, int i, int j){
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    public void remove() {

    }
    
	public static Set<String> realPLs(Set<String> pls, int n) {
		
		Set<Set<String>> ultimate_tmp = new HashSet<Set<String>>();
		//System.out.println("Number of products: " + pls.size() + "\n");
		
		for(String conf : pls) {
			//System.out.println("Current product: " + conf);

			Set<String> set = new HashSet<String>();
			for(String p : conf.replace("}{", "##").replace("{", "").replace("}", "").split("##")) {
				Set<String> tmp = new HashSet<String>();
				for(String f : p.trim().split(" ")) {
					tmp.add(f);
				}
				
				String product = "";
				
				for(String f : tmp) {
					product += f + " ";
				}
				
				set.add(product.trim());
			}
			ultimate_tmp.add(set);
		}
		
		Integer[] ints = new Integer[n-1];
		for(int i = 1; i < n; ++i) {
			ints[i-1] = (i);
		}

		Set<Set<String>> ultimate_tmp2 = new HashSet<Set<String>>();
		
		for(Set<String> configuration : ultimate_tmp) {
			
			Permutations<Integer> perm = new Permutations<Integer>(ints);
			while(perm.hasNext()) {
				Integer[] transform = perm.next();
				Set<String> new_product = new HashSet<String>();
				for(String p : configuration) {
					//System.out.println("p = " + p);
					for(int i = 0; i < transform.length; ++i) {
						p = p.replace("" + (i+1), Character.toString((char)('A'+ i)));
					}
					
					for(int i = 0; i < transform.length; ++i) {
						p = p.replace(Character.toString((char)('A'+ i)), "" + transform[i]);
					}
					//System.out.println("p after replace = " + p);
					
					Set<String> tmp = new HashSet<String>();
					for(String str : p.split(" ")) {
						tmp.add(str);
					}
					p = "";
					for(String str : tmp) {
						p += str + " ";
					}
					new_product.add(p.trim());
				}
				ultimate_tmp2.add(new_product);
			}
		}
		
		ultimate_tmp.addAll(ultimate_tmp2);
		
		Set<String> result = new HashSet<String>();
		for(Set<String> conf : ultimate_tmp) {
			String str = "";
			for(String c : conf) {
				str += "{" + c + "}";
			}
			result.add(str);
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		for(int i = 2; i <= 6; ++i ) {
			try {
				Set<String> result = realPLs(new HashSet<String>(Files.readAllLines(Paths.get("result"+i+".txt"))), i);
				Files.write(Paths.get("result"+i+"_real.txt"), result, Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}