package it.polito.tdp.itunes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import it.polito.tdp.itunes.db.ItunesDAO;

import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;

public class Model {
	
	private Graph<Album,DefaultEdge> grafo;
	private ItunesDAO dao;
	private Map<Integer,Album> allAlbums;
	private List<Album> albumDurationOverd;
	
	private int dimMax;
	private List<Album> setMax;

	
	
	public Model() {
		this.dao = new ItunesDAO();
		this.allAlbums = new HashMap<Integer, Album>();
		this.allAlbums = dao.getAllAlbums();
		/*for(Album a : allAlbums.values()) {
			a.setListaCanzoni(dao.getAllTracksForAlbum(a.getAlbumId()));
		}*/
	}
	
	public void createGraph(int d) {
		this.grafo = new SimpleGraph<>(DefaultEdge.class);
		albumDurationOverd = dao.getAllAlbumsWithDurationOverd(d);
		
		 
		//Aggiungo i vertici
		Graphs.addAllVertices(this.grafo, albumDurationOverd);
		
		//Aggiungo gli archi
		Map<Integer, Album> albumIdMap = new HashMap<>();
		for(Album a: this.grafo.vertexSet()) {
			albumIdMap.put(a.getAlbumId(), a) ;
		}
		List<Pair<Integer,Integer>> archi = dao.getCompatibleAlbums();
		for(Pair<Integer, Integer>arco: archi) {
			if(albumIdMap.containsKey(arco.getFirst()) && 
					albumIdMap.containsKey(arco.getSecond()) &&
					!arco.getFirst().equals(arco.getSecond())) {
				this.grafo.addEdge(albumIdMap.get(
						arco.getFirst()), albumIdMap.get(arco.getSecond()));
			}
		}
		
	}
	
	public Set<Album> ricercaSetMassimo(Album a1, double dTot){
		//Predispongo la prima chiamata e ricevo il risultato
		
		if(a1.getDurata() > dTot) {
			return null;
		}
		
		List<Album> parziale = new ArrayList<>();
		parziale.add(a1);
		List<Album> tutti = new ArrayList<>(getComponenteConnessa(a1));
		tutti.remove(a1);
		
		dimMax = 1;
		setMax = new ArrayList<>(parziale);
		
		cerca(parziale, 1, dTot, tutti, a1.getDurata());
		
		return new HashSet<>(setMax);
		
		
	}
	
	public void cerca(List<Album> parziale,int livello, double dTot, List<Album> tutti, double durataParziale) {
		
		//Ogni soluzine parziale potenzialmente è quella finale
		//non conosco infatti a priori la dimensione della soluzione finale
		if(parziale.size() > dimMax) {
			dimMax = parziale.size();
			setMax = new ArrayList<Album>(parziale);
		}
		
		for(Album nuovo : tutti) {
			if(nuovo.getAlbumId()>parziale.get(parziale.size()-1).getAlbumId() && durataParziale + nuovo.getDurata() <= dTot) {
				parziale.add(nuovo);
				cerca(parziale, livello+1, dTot, tutti, durataParziale+nuovo.getDurata());
				parziale.remove(parziale.size()-1);
			}
		}
	}
	
	
   public int getNVertici() {
	   return this.grafo.vertexSet().size();
   }
   
   public List<Album> getAlbums(){
	   List<Album> list = new ArrayList<Album>(this.grafo.vertexSet());
	   Collections.sort(list);
	   return list;
   }
   
   public int getNArchi() {
	   return this.grafo.edgeSet().size();
   }
   
   public Set<Album> getComponenteConnessa(Album a1){
	   ConnectivityInspector<Album, DefaultEdge> ci = new ConnectivityInspector<>(this.grafo);
	   return ci.connectedSetOf(a1); 
   }
	
	
}
