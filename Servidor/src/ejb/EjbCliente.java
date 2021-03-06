package ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUtil;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import datatypes.DTEstado;
import datatypes.DTPregunta;
import datatypes.DTPreguntaConRespuesta;
import datatypes.DTRespuesta;
import ejb.remote.IEjbClienteRemote;
import model.Accion;
import model.Cliente;
import model.ClientePaquete;
import model.Historico;
import model.JPAProveedorValor;
import model.Paquete;
import model.PaqueteAlgoritmico;
import model.PaqueteCategoria;
import model.Pregunta;
import model.PreguntaEspecifica;
import model.PreguntaGeneral;
import model.Respuesta;
import model.RespuestaEspecifica;
import model.RespuestaGeneral;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import model.PaqueteIndice;
import model.Portafolio;


@Local(IEjbCliente.class)
@Stateless
public class EjbCliente implements IEjbCliente,  IEjbClienteRemote {

	@PersistenceContext(unitName = "Servidor")
	private EntityManager em;

	@EJB
	private IEjbPaquete ejbPaquete;
	
	@EJB
	private IEjbServidor ejbServidor;

	@Override
	public DTEstado ajustarPaquetesEnBaseRespGenerales(String idCliente, String respuestasGeneralesString) {
		Query q1 = em.createQuery("select c from Cliente c where c.idTelefono=:id");
		q1.setParameter("id", idCliente);

		if (q1.getResultList().size() == 0)
			return new DTEstado("Error: idCliente no existe");

		Cliente c = (Cliente) q1.getSingleResult();

		if (c.getRespuestas().size() > 0)
			return new DTEstado("Error: Ya respondio preguntas (Necesita resetarlas para volver a contestar)");

		// parseo de string
		String[] idResp = respuestasGeneralesString.split("-");
		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < idResp.length; i++) {
			ids.add(Long.parseLong(idResp[i]));
		}

		TypedQuery<RespuestaGeneral> q2 = em.createQuery("select rg from RespuestaGeneral rg where rg.id IN :listaResp",RespuestaGeneral.class);
		q2.setParameter("listaResp", ids);
		List<RespuestaGeneral> respuestasGenerales =  q2.getResultList();

		Set<PaqueteCategoria> categoriasConjunto = new HashSet<PaqueteCategoria>();

		// Formo un conjunto con todas las categorias obtenidas de sus
		// respuestas

		for (RespuestaGeneral rg : respuestasGenerales) {
			categoriasConjunto.addAll(rg.getCategorias());
		}
		// Obtengo todos los paquetes del sistema que cumplen con esas
		// categorias
		List<Paquete> paquetes = ejbPaquete
				.obtenerPaquetesPorCategoria(new ArrayList<PaqueteCategoria>(categoriasConjunto),c.getPortafolio().getUltimaFechaSaldo());

		// Le asigno las respuestas al cliente

		c.getRespuestas().addAll(respuestasGenerales);

		// Le asigno al portafolio todos esos paquetes pero con inversion 0
		// porque aun no respondio las especificas
		for (Paquete p : paquetes) {
			ClientePaquete cp = ClientePaquete.generar(c, p, 0);
		}

		return new DTEstado("ok");
	}

	@SuppressWarnings("unchecked")
	@Override
	public DTEstado ajustarInversionPaquetesEnBaseRespEspecificas(String idCliente, String respuestas) {
		System.out.println("INICIO de ajuste de inversion en base  resp. esp.");

		Query q1 = em.createQuery("select c from Cliente c where c.idTelefono=:id");
		q1.setParameter("id", idCliente);

		if (q1.getResultList().size() == 0)
			return new DTEstado("Error: idCliente no existe");

		Cliente c = (Cliente) q1.getSingleResult();

		if (c.getRespuestas().size() == 0)
			return new DTEstado("Error: No has respondido las generales aun");

		for (Respuesta r : c.getRespuestas()) {
			if (r.getTipoRespuesta().toLowerCase().equals("especifica"))
				return new DTEstado(
						"Error: Ya respondio preguntas especificas (Necesita resetarlas para volver a contestar)");

		}

		String[] idResp = respuestas.split("-");

		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < idResp.length; i++) {
			ids.add(Long.parseLong(idResp[i]));
		}

		Query q2 = em.createQuery("select re from RespuestaEspecifica re where re.id IN :listaResp");
		q2.setParameter("listaResp", ids);

		List<RespuestaEspecifica> respuestasEspecificas = q2.getResultList();

		HashMap<Long, RespuestaEspecifica> respuestasEspecificasConIdPreg = new HashMap<Long, RespuestaEspecifica>();

		for (RespuestaEspecifica re : respuestasEspecificas) {
			respuestasEspecificasConIdPreg.put(re.getPregunta().getId(), re);
		}

		c.getPortafolio().ajustarInversionPaquetesEnBaseRespEspecificas(respuestasEspecificasConIdPreg,new JPAProveedorValor(em));
		System.out.println("FIN de ajuste de inversion en base  resp. esp.");
		return new DTEstado("ok");
	}

	@Override
	public ArrayList<DTPregunta> obtenerPreguntasEspecificas(String idCliente) {
		ArrayList<DTPregunta> preguntasDT = new ArrayList<DTPregunta>();

		Query q1 = em.createQuery("select c from Cliente c where c.idTelefono=:id");
		q1.setParameter("id", idCliente);
		if (q1.getResultList().size() == 0)
			return preguntasDT;

		Cliente c = (Cliente) q1.getSingleResult();

		for (Respuesta r : c.getRespuestas()) {
			if (r.getTipoRespuesta().toLowerCase().equals("especifica"))
				return preguntasDT;
		}

		List<PreguntaEspecifica> preguntasEspecificas = c.obtenerPreguntasEspecificas();

		for (PreguntaEspecifica pg : preguntasEspecificas) {
			ArrayList<DTRespuesta> dtRespuestas = new ArrayList<DTRespuesta>();
			for (Respuesta r : pg.getRespuestas()) {
				DTRespuesta dtr = new DTRespuesta(r.getId(), r.getSentenciaENG(), r.getSentenciaESP());
				dtRespuestas.add(dtr);
			}
			DTPregunta dtpg = new DTPregunta(pg.getId(), pg.getSentenciaENG(), pg.getSentenciaESP(), dtRespuestas);

			preguntasDT.add(dtpg);
		}

		return preguntasDT;
	}

	private Accion cargarDatosHistoricosAccion(String symbol) throws IOException {
		Stock stock;

		stock = YahooFinance.get(symbol, new GregorianCalendar(2014, Calendar.JANUARY, 1), Interval.DAILY);
		Accion a = em.createQuery("Select from Accion a where a.symbol = :s", Accion.class).setParameter("s", symbol)
				.getSingleResult();
		// a.setNombre(stock.getName());
		// a.setSymbol(stock.getSymbol());
		a.setValorActual(stock.getQuote().getPrice().doubleValue());
		for (HistoricalQuote hq : stock.getHistory()) {
			Historico h = new Historico(a, hq.getAdjClose().doubleValue(), hq.getClose().doubleValue(),
					hq.getDate().getTime());
			a.getHistoricos().add(em.merge(h));
		}
		Historico actual = new Historico(a, a.getValorActual(), a.getValorActual(), Calendar.getInstance().getTime());
		a.getHistoricos().add(actual);
		em.flush();
		return a;
	}

	

	@Override
	public String registrarUsuario(String identificador) {
		Query q = em.createQuery("select c from Cliente c where c.idTelefono = :idTel").setParameter("idTel",
				identificador);
		System.out.println("Aca toy");
		// Verifico si ya existe un cliente con ese idTelefono
		if (identificador.length() == 0) {
			return "Error: id vacio";
		} else if (q.getResultList().size() > 0) {
			return "Error: Ese id ya esta registrado en el sistema";
		} else {
			Cliente nuevoCliente = new Cliente();
			nuevoCliente.setIdTelefono(identificador);
			nuevoCliente.getPortafolio().asignarSaldoInicial();
			em.persist(nuevoCliente);

			return "ok";
		}
	}

	@Override
	public ArrayList<DTPreguntaConRespuesta> obtenerPreguntasRespondidasPorCliente(String idCliente) {
		ArrayList<DTPreguntaConRespuesta> dtPreguntasConRespuestas = new ArrayList<DTPreguntaConRespuesta>();

		Query q = em.createQuery("select c from Cliente c where c.idTelefono = :idTel").setParameter("idTel",
				idCliente);

		if (q.getResultList().size() == 0)
			return dtPreguntasConRespuestas;

		Cliente c = (Cliente) q.getSingleResult();

		for (Respuesta resp : c.getRespuestas()) {
			DTPreguntaConRespuesta dtPregConResp = new DTPreguntaConRespuesta();
			dtPregConResp.setIdRespuestaUsuario(resp.getId());

			Pregunta preg = resp.getPregunta();

			ArrayList<DTRespuesta> dtRespuestasPregunta = new ArrayList<DTRespuesta>();

			for (Respuesta r : preg.getRespuestas()) {
				DTRespuesta dtResp = new DTRespuesta(r.getId(), r.getSentenciaENG(), r.getSentenciaESP());
				dtRespuestasPregunta.add(dtResp);
			}

			DTPregunta dtpreg = new DTPregunta(preg.getId(), preg.getSentenciaENG(), preg.getSentenciaESP(),
					dtRespuestasPregunta);

			dtPregConResp.setPregunta(dtpreg);

			dtPreguntasConRespuestas.add(dtPregConResp);

		}

		return dtPreguntasConRespuestas;
	}

	@Override
	public ArrayList<ClientePaquete> obtenerClientesConPaqueteAlgoritmico(long idPaquete) {
		System.out.println("idpaquete=" + idPaquete);
		Query q = em.createQuery("select cp from ClientePaquete cp where cp.paquete.id=:idpaq");
		q.setParameter("idpaq", idPaquete);
		ArrayList<ClientePaquete> clientesPaquetes = (ArrayList<ClientePaquete>) q.getResultList();
		System.out.println("paso la consulta");
		return clientesPaquetes;
	}

	@Override
	public DTEstado resetearTodasLasPreguntas(String idCliente) {
		Query q1 = em.createQuery("select c from Cliente c where c.idTelefono=:id");
		q1.setParameter("id", idCliente);

		if (q1.getResultList().size() == 0)
			return new DTEstado("Error: idCliente no existe");

		Cliente c = (Cliente) q1.getSingleResult();

		List<ClientePaquete> clientePaquetesABorrar = new ArrayList<ClientePaquete>(c.getPortafolio().getPaquetes());

		c.getPortafolio().ReseterPaquetes();
		c.getRespuestas().clear();

		for (Iterator<ClientePaquete> ip = clientePaquetesABorrar.iterator(); ip.hasNext();) {
			ClientePaquete cp = (ClientePaquete) ip.next();

			ip.remove();
			em.remove(em.contains(cp) ? cp : em.merge(cp));
		}
		em.flush();

		ejbServidor.intentarBorrarPreguntasGeneralesDeprecated();
		ejbPaquete.intentarBorrarPaquetesDeprecated();
		return new DTEstado("ok");
	}

	@Override
	public void borrarSaldoHistorico(Portafolio p){
		Query q = em.createQuery("DELETE FROM SaldoHistorico s Where s.portafolio = :p");
		q.setParameter("p", p);
		q.executeUpdate();
	}

	@Override
	public DTEstado borrarCliente(String idCliente) {
		Query q1 = em.createQuery("select c from Cliente c where c.idTelefono=:id");
		q1.setParameter("id", idCliente);

		if (q1.getResultList().size() == 0)
			return new DTEstado("Error: idCliente no existe");

		Cliente c = (Cliente) q1.getSingleResult();
		borrarSaldoHistorico(c.getPortafolio());
		em.remove(c);
		
		ejbServidor.intentarBorrarPreguntasGeneralesDeprecated();
		ejbPaquete.intentarBorrarPaquetesDeprecated();
		
		
		return new DTEstado("ok cliente borrado");

	}
	
	@Override
	public DTEstado obtenerInfoCliente(String id){
		TypedQuery<Portafolio> q = em.createQuery("select p from Portafolio p where p.cliente.idTelefono=:id",Portafolio.class);
		q.setParameter("id",id);
		List<Portafolio> lp = q.getResultList();
		if (lp.isEmpty()) return new DTEstado("No user with such id");
		Portafolio p = lp.get(0);
		String res = "Historic: " + Boolean.toString(p.isHistorico()) + ", Active: " + Boolean.toString(p.isActivo()) + ", has Lost:" + Boolean.toString(p.isLost()) + "\n";
		Collection<ClientePaquete> lst = p.getPaquetes();
		for(ClientePaquete cp : lst){
			res += "inv=" +  cp.getInversion() + " paquete id=" + cp.getPaquete().getId() + " paq nombre=" + cp.getPaquete().getNombre() + "\n";
		}
		return new DTEstado(lst.isEmpty() ? res + "No packets" : res);
	}
	


}
