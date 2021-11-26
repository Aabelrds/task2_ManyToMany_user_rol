package com.formacionjava.springboot.apirest.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.formacionjava.springboot.apirest.models.entity.Cliente;
import com.formacionjava.springboot.apirest.models.entity.Region;
import com.formacionjava.springboot.apirest.models.services.ClienteService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api")
@Api(tags = "demo")
public class ClienteRestController {

	@Autowired
	private ClienteService clienteService;

	@GetMapping("/clientes")
	public List<Cliente> index() {
		return clienteService.findAll();
	}

	/*
	 * @GetMapping("/clientes/{id}") public Cliente show(@PathVariable Long id) {
	 * return clienteService.findById(id); }
	 */
	@GetMapping("/clientes/{id}")
	public ResponseEntity<?> show(@PathVariable Long id) {
		Cliente cliente = null;
		Map<String, Object> response = new HashMap<>();

		try {
			cliente = clienteService.findById(id);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al realizar consulta en base de datos");
			response.put("error", e.getMessage().concat(" : ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (cliente == null) {
			response.put("mensaje",
					"El cliente con ID : ".concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
	}

	/*
	 * @PostMapping("/clientes")
	 * 
	 * @ResponseStatus(HttpStatus.CREATED) public Cliente create(@RequestBody
	 * Cliente cliente) {
	 * 
	 * return clienteService.save(cliente); }
	 */

	@PostMapping("/clientes")
	public ResponseEntity<?> create(@RequestBody Cliente cliente) {

		Cliente clienteNew = null;
		Map<String, Object> response = new HashMap<>();

		try {
			clienteNew = clienteService.save(cliente);
		} catch (DataAccessException e) {
			response.put("message", "Error al realizar insert en base de datos");
			response.put("error", e.getMessage().concat(" : ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		response.put("mensaje", "Cliente creado con éxito");
		response.put("cliente", clienteNew);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	/*
	 * @PutMapping("/clientes/{id}")
	 * 
	 * @ResponseStatus(HttpStatus.CREATED) public Cliente update(@RequestBody
	 * Cliente cliente, @PathVariable Long id) { Cliente clienteUpdate =
	 * clienteService.findById(id);
	 * 
	 * clienteUpdate.setApellido(cliente.getApellido());
	 * clienteUpdate.setNombre(cliente.getNombre());
	 * clienteUpdate.setEmail(cliente.getEmail());
	 * 
	 * return clienteService.save(clienteUpdate); }
	 */

	@PutMapping("/clientes/{id}")
	public ResponseEntity<?> update(@RequestBody Cliente cliente, @PathVariable Long id) {

		Cliente clienteActual = clienteService.findById(id);

		Map<String, Object> response = new HashMap<>();
		Cliente clienteUpdated = null;

		if (clienteActual == null) {
			response.put("mensaje", "Error: No se puede editar, el cliente ID : "
					.concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		try {
			clienteActual.setNombre(cliente.getNombre());
			clienteActual.setApellido(cliente.getApellido());
			clienteActual.setEmail(cliente.getEmail());
			clienteActual.setTelefono(cliente.getTelefono());
			clienteActual.setRegion(cliente.getRegion());

			if (cliente.getCreatedAt() == null) {

				clienteActual.setCreatedAt(clienteActual.getCreatedAt());

			} else {

				clienteActual.setCreatedAt(cliente.getCreatedAt());
			}

			clienteUpdated = clienteService.save(clienteActual);

		} catch (DataAccessException e) {
			response.put("message", "Error al actualizar al cliente en base de datos");
			response.put("error", e.getMessage().concat(" : ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		response.put("mensaje", "Cliente actualizado con éxito");
		response.put("cliente", clienteUpdated);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	/*
	 * @DeleteMapping("clientes/{id}") public void delete(@PathVariable Long id) {
	 * clienteService.delete(id); }
	 */
	@DeleteMapping("clientes/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();

		try {
			clienteService.delete(id);
		} catch (DataAccessException e) {
			response.put("message", "Error al eliminar al cliente en base de datos");
			response.put("error", e.getMessage().concat(" : ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente se ha eliminado");

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@PostMapping("clientes/upload")
	public ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile archivo, @RequestParam("id") Long id) {
		Map<String, Object> response = new HashMap<>();

		Cliente cliente = clienteService.findById(id);

		if (!archivo.isEmpty()) {
			String nombreArchivo = UUID.randomUUID().toString() + "-" + archivo.getOriginalFilename().replace(" ", "");
			Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo).toAbsolutePath();

			try {
				Files.copy(archivo.getInputStream(), rutaArchivo);

			} catch (IOException e) {

				response.put("message", "Error al subir la imagen del cliente");
				response.put("error", e.getMessage().concat(" : ").concat(e.getCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);

			}

			String nombreFotoAnterior = cliente.getImagen();

			if (nombreFotoAnterior != null) {

				Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
				File archivoAnterior = rutaFotoAnterior.toFile();
				archivoAnterior.delete();
				System.out.println(archivoAnterior + " Archivo eliminado");

			}
			cliente.setImagen(nombreArchivo);
			clienteService.save(cliente);

			response.put("mensaje", "Imagen guardada correctamente " + nombreArchivo);
			response.put("cliente", cliente);

		}
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String nombreFoto) {

		Path rutaArchivo = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();

		Resource recurso = null;

		try {
			recurso = new UrlResource(rutaArchivo.toUri());

		} catch (MalformedURLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (!recurso.exists()) {
			throw new RuntimeException("Error no se puede cargar la imagen " + nombreFoto);

		}

		HttpHeaders cabecera = new HttpHeaders();
		cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + recurso.getFilename() + "\"");

		return new ResponseEntity<Resource>(recurso, cabecera, HttpStatus.OK);
	}
	
	@GetMapping("clientes/regiones")
	public List<Region> listaRegiones(){
		
		return clienteService.findAllRegions();
	};
	
}
