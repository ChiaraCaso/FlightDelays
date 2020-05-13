package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	//grafo semplice, pesato e non orientato
	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private Map<Integer, Airport> idMap ;
	private ExtFlightDelaysDAO dao;
	
	private Map <Airport, Airport> visita = new HashMap<Airport, Airport>();
	
	public Model() {
		idMap = new HashMap<Integer, Airport>();
		dao = new ExtFlightDelaysDAO();
		this.dao.loadAllAirports(idMap);
		
	}
	
	public void creaGrafo(int x) {
		this.grafo = new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//AGGIUNGIAMO I VERTICI
		for(Airport a : idMap.values()) {
			
			if(dao.getAirlinesNumber(a) > x) {
				//inserisco l'aereoporto come vertice
				this.grafo.addVertex(a);
			}
		}
			
			for(Rotta r : dao.getRotte(idMap)) {
				
				if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				
					DefaultWeightedEdge e = this.grafo.getEdge(r.a1, r.a2);
				
					if(e == null) {
						//Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getPeso());
						Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getPeso());
				} else {
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getPeso();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
	}
	
	public int vertexNumber() {
		return this.grafo.vertexSet().size();
	}
	
	public int edgeNumber () {
		return this.grafo.edgeSet().size();
	}
	
	public Collection <Airport> getAereoporti (){
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso (Airport a1, Airport a2) {
		List <Airport> percorso = new ArrayList<Airport>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo, a1);
		//aggiungo la radice del mio albero di visita
		visita.put(a1, null);
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport sorgente = grafo.getEdgeSource(e.getEdge());
				Airport destinazione = grafo.getEdgeTarget(e.getEdge());
				
				if(!visita.containsKey(destinazione) && visita.containsKey(sorgente)) {
					visita.put(destinazione, sorgente);
				} else if(!visita.containsKey(sorgente) && visita.containsKey(destinazione)) {
					visita.put(sorgente, destinazione);
				}
				
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			}
		});
		
		while(it.hasNext()) {
			it.next();
		}
		
		if(!visita.containsKey(a1) || !visita.containsKey(a2)) {
			//i due aereoporti non sono collegati 
			return null;
		}
		
		Airport step = a2;
		
		//salgo finchè non ritrovo la partenza
		while(!step.equals(a1)) {
			percorso.add(step);
			step = visita.get(step);
		}
		
		percorso.add(a1);
		
		return percorso;
	}
}
