package com.example.restaurante;

import com.example.restaurante.entities.*;
import com.example.restaurante.repositories.CategoriaRepository;
import com.example.restaurante.repositories.UnidadMedidaRepository;
import com.example.restaurante.services.ArticuloInsumoService;
import com.example.restaurante.services.ArticuloManufacturadoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.*;

@SpringBootApplication
public class RestauranteApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestauranteApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(
			UnidadMedidaRepository unidadMedidaRepository,
			CategoriaRepository categoriaRepository,
			ArticuloInsumoService articuloInsumoService,
			ArticuloManufacturadoService articuloManufacturadoService
	) {
		return args -> {
			Scanner scanner = new Scanner(System.in);
			boolean ejecutando = true;

			//lista de almacenamiento de insumos
			List<ArticuloInsumo> insumosCreados = new ArrayList<>();

			//menu
			while(ejecutando){
				System.out.println("Gestion Restaurante");
				System.out.println("1. Agregar un nuevo insumo");
				System.out.println("2. Actualizar stock de insumo existente");
				System.out.println("3. Listar insumos");
				System.out.println("4. Eliminar insumo");
				System.out.println("5. Crear nueva categoria");
				System.out.println("6. Listar categorias");
				System.out.println("7. Eliminar categoria");
				System.out.println("8. Crear articulo manufacturado");
				System.out.println("9. Listar articulos manufacturados");
				System.out.println("10. Eliminar articulo manufacturado");
				System.out.println("11. Ver detalles de artículo manufacturado");
				System.out.println("12. Gestionar unidades de medida");
				System.out.println("13. Salir");
				System.out.println("Ingrese la opcion: ");

				int opcion = 0;
				try {
					opcion = Integer.parseInt(scanner.nextLine());
				} catch (NumberFormatException e) {
					System.out.println("El numero ingresado no existe, por favor ingrese una opcion del 1 al 13");
					continue;
				}

				switch (opcion){
					case 1:
						//agrego nuevo insumo
						System.out.print("Nombre del insumo: ");
						String nombreInsumo = scanner.nextLine();

						//verifico si ya existe este insumo
						boolean insumoExistente = false;
						for (ArticuloInsumo insumo : articuloInsumoService.findAll()){
							if (insumo.getDenominacion().equalsIgnoreCase(nombreInsumo)){
								insumoExistente = true;
								System.out.println("Insumo ya existente en la base de datos" +
										", desea crearlo de todos modos? (s/n)");
								String continuar = scanner.nextLine();
								if (!continuar.equalsIgnoreCase("s")){
									break;
								} else {
									insumoExistente = false; //si escribe s, permite continuar
									break;
								}
							}
						}

						if (!insumoExistente || scanner.nextLine().equalsIgnoreCase("s")){
							ArticuloInsumo nuevoInsumo = crearNuevoInsumo (scanner,nombreInsumo,unidadMedidaRepository);
							if (nuevoInsumo != null){
								articuloInsumoService.save(nuevoInsumo);
								insumosCreados.add(nuevoInsumo);
								System.out.println("Insumo agregado");
							}
						}
						break;
					case 2:
						//actualizar stock (incrementando)
						List<ArticuloInsumo> insumosExistentes = articuloInsumoService.findAll();
						if (insumosExistentes.isEmpty()){
							System.out.println("No hay insumos para actualizar");
							break;
						}

						listarInsumos(insumosExistentes);
						System.out.println("Ingrese el ID del insumo a actualizar: ");
						try {
							int insumoId = Integer.parseInt(scanner.nextLine());
							ArticuloInsumo insumoAActualizar = null;

							for (ArticuloInsumo insumo : insumosExistentes){
								if (insumo.getId() == insumoId){
									insumoAActualizar = insumo;
									break;
								}
							}

							if (insumoAActualizar == null){
								System.out.println("Insumo no encontrado");
								break;
							}

							System.out.println("Insumo a actualizar: " + insumoAActualizar.getDenominacion());
							System.out.println("Stock actual: " + insumoAActualizar.getStockActual() + " " +
									insumoAActualizar.getUnidadMedida().getDenominacion());
							System.out.println("Stock maximo: " + insumoAActualizar.getStockMaximo() + " " +
									insumoAActualizar.getUnidadMedida().getDenominacion());

							System.out.println("Ingrese la cantidad a ingresar del insumo: ");
							int incremento = Integer.parseInt(scanner.nextLine());

							if (incremento <= 0){
								System.out.println("El incremento debe ser mayor a cero");
								break;
							}

							int nuevoStock = insumoAActualizar.getStockActual() + incremento;

							//verifico que el nuevo stock no sea mayor al maximo permitido
							if (nuevoStock > insumoAActualizar.getStockMaximo()){
								System.out.println("El nuevo stock " + nuevoStock + ") supera el maximo permitido (" +
										insumoAActualizar.getStockMaximo() + ")");
								System.out.println("Desea modificar el stock maximo? (s/n)");
								String ajustar = scanner.nextLine();

								if (ajustar.equalsIgnoreCase("s")){
									nuevoStock = insumoAActualizar.getStockActual();
									System.out.println("Stock ajustado al maximo permitido");
								} else {
									System.out.println("Operacion nula");
									break;
								}
							}

							insumoAActualizar.setStockActual(nuevoStock);
							articuloInsumoService.save(insumoAActualizar);
							System.out.println("Nuevo stock: " + insumoAActualizar.getStockActual());
						} catch (NumberFormatException e){
							System.out.println("Numero ingresado no valido");
						}
						break;
					case 3:
						//lista de insumos
						listarInsumos(articuloInsumoService.findAll());
						break;
					case 4:
						//elimina insumo
						eliminarInsumo(scanner, articuloInsumoService);
						break;
					case 5:
						//creo nueva categoria
						System.out.println("Ingrese el nombre de la categoria: ");
						String nombreCategoria = scanner.nextLine();
						Categoria nuevaCategoria = crearCategoria(nombreCategoria, categoriaRepository);
						System.out.println("Categoria " + nuevaCategoria.getDenominacion() + " creada, con ID "
								+ nuevaCategoria.getId());
						break;
					case 6:
						//lista de categorias
						listarCategorias(categoriaRepository.findAll());
						break;
					case 7:
						//eliminar categoría
						eliminarCategoria(scanner, categoriaRepository);
						break;
					case 8:
						//creo articulo manufacturado
						if (insumosCreados.isEmpty() && articuloInsumoService.findAll().isEmpty()){
							System.out.println("No hay insumos para elaborar el producto, ingrese los insumos primero");
						} else {
							List<Categoria> categorias = categoriaRepository.findAll();
							if (categorias.isEmpty()){
								System.out.println("No hay categorias disponibles, cree una categoria primero");
							} else {
								crearArticuloManufacturado (scanner,articuloInsumoService,articuloManufacturadoService,
										categorias);
							}
						}
						break;
					case 9:
						//lista de articulos manufacturados
						listarArticulosManufacturados(articuloManufacturadoService.findAll());
						break;
					case 10:
						//elimina artículo manufacturado
						eliminarArticuloManufacturado(scanner, articuloManufacturadoService);
						break;
					case 11:
						//detalles de articulos manufacturados
						verDetallesArticuloManufacturado(scanner, articuloManufacturadoService);
						break;
					case 12:
						//unidad de medida
						gestionarUnidadesMedida(scanner, unidadMedidaRepository);
						break;
					case 13:
						//salir
						ejecutando = false;
						break;
					default:
						System.out.println("Opcion no valida, ingrese nuevamente una opcion entre el 1 y el 10");
				}
			}
			scanner.close();
		};
	}

	private static void gestionarUnidadesMedida (Scanner scanner, UnidadMedidaRepository repository){
		boolean volver = false;

		while (!volver) {
			//muestro las unidades ya existentes
			List<UnidadMedida> unidadesExistentes = repository.findAll();

			if (unidadesExistentes.isEmpty()) {
				System.out.println("No hay unidades de medida registradas.");
			} else {
				System.out.println("Unidades de medida existentes:");
				System.out.printf("%-5s %-30s\n", "ID", "Nombre");
				System.out.println("-----------------------------------");

				for (UnidadMedida unidad : unidadesExistentes) {
					System.out.printf("%-5d %-30s\n",
							unidad.getId(),
							unidad.getDenominacion());
				}
			}

			System.out.println("\nOpciones:");
			System.out.println("1. Crear nueva unidad de medida");
			System.out.println("2. Volver al menu principal");
			System.out.print("Ingrese su opción: ");

			try {
				int opcion = Integer.parseInt(scanner.nextLine());

				switch (opcion) {
					case 1:
						//creo nueva unidad
						System.out.print("\nIngrese el nombre de la nueva unidad de medida: ");
						String nombreUnidad = scanner.nextLine();

						//verifico si ya existe
						boolean yaExiste = false;
						for (UnidadMedida um : unidadesExistentes) {
							if (um.getDenominacion().equalsIgnoreCase(nombreUnidad)) {
								System.out.println("¡Esta unidad de medida ya existe con ID: " + um.getId() + "!");
								yaExiste = true;
								break;
							}
						}

						if (!yaExiste) {
							UnidadMedida nuevaUnidad = new UnidadMedida();
							nuevaUnidad.setDenominacion(nombreUnidad);
							nuevaUnidad = repository.save(nuevaUnidad);
							System.out.println("Unidad de medida '" + nuevaUnidad.getDenominacion() +
									"' creada con ID: " + nuevaUnidad.getId());
						}
						break;

					case 2:
						//menu ppal
						volver = true;
						break;

					default:
						System.out.println("Opcion no valida. Por favor intente de nuevo.");
				}

			} catch (NumberFormatException e) {
				System.out.println("Por favor, ingrese un numero valido.");
			}
		}
	}

	private static Categoria crearCategoria (String nombre, CategoriaRepository repository){
		Categoria categoria = new Categoria();
		categoria.setDenominacion(nombre);
		return repository.save(categoria);
	}

	private static ArticuloInsumo crearNuevoInsumo (Scanner scanner, String nombre, UnidadMedidaRepository unidadMedidaRepository){
		ArticuloInsumo insumo = new ArticuloInsumo();

		System.out.println("Nuevo insumo");

		insumo.setDenominacion(nombre);

		System.out.println("Precio de venta: ");
		try {
			insumo.setPrecioVenta(Double.parseDouble(scanner.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("El precio debe ser un numero valido");
			return null;
		}

		System.out.println("Precio de compra: ");
		try {
			insumo.setPrecioCompra(Double.parseDouble(scanner.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("El precio debe ser un numero valido");
			return null;
		}

		System.out.println("Stock actual: ");
		try {
			insumo.setStockActual(Integer.parseInt(scanner.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("El stock debe ser un numero entero");
			return null;
		}

		System.out.println("Stock maximo: ");
		try {
			insumo.setStockMaximo(Integer.parseInt(scanner.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("El stock debe ser un numero entero");
			return null;
		}

		System.out.println("Es para elaborar? (s/n): ");
		String esParaElaborar = scanner.nextLine();
		insumo.setEsParaElaborar(esParaElaborar.equalsIgnoreCase("s"));

		//unidades de medida disponible
		List<UnidadMedida> unidadesDisponibles = unidadMedidaRepository.findAll();

		if (unidadesDisponibles.isEmpty()) {
			System.out.println("Error: No hay unidades de medida disponibles. Por favor, cree alguna unidad de medida primero.");
			return null;
		}

		System.out.println("Seleccione unidad de medida:");
		System.out.printf("%-5s %-30s\n", "ID", "Nombre");
		System.out.println("-----------------------------------");

		for (UnidadMedida unidad : unidadesDisponibles) {
			System.out.printf("%-5d %-30s\n",
					unidad.getId(),
					unidad.getDenominacion());
		}

		System.out.print("\nIngrese el ID de la unidad de medida: ");
		try {
			int unidadId = Integer.parseInt(scanner.nextLine());

			UnidadMedida unidadSeleccionada = null;
			for (UnidadMedida unidad : unidadesDisponibles) {
				if (unidad.getId() == unidadId) {
					unidadSeleccionada = unidad;
					break;
				}
			}

			if (unidadSeleccionada == null) {
				System.out.println("Error: Unidad de medida no encontrada.");
				return null;
			}

			insumo.setUnidadMedida(unidadSeleccionada);
		} catch (NumberFormatException e) {
			System.out.println("Error: ID no válido.");
			return null;
		}
		return insumo;
	}

	private static void listarInsumos(List<ArticuloInsumo> insumos){
		if (insumos.isEmpty()){
			System.out.println("No hay insumos para mostrar");
			return;
		}

		System.out.println("Lista de insumos");
		System.out.printf("%-5s %-30s %-10s %-15s %-15s\n", "ID", "Nombre", "Stock", "Unidad", "Precio Venta");
		System.out.println("-----------------------------------------------------------------------");

		for (ArticuloInsumo insumo : insumos){
			System.out.printf("%-5d %-30s %-10d %-15s $%-15.2f\n",
					insumo.getId(),
					insumo.getDenominacion(),
					insumo.getStockActual(),
					insumo.getUnidadMedida().getDenominacion(),
					insumo.getPrecioVenta());
		}
	}

	private static void listarCategorias(List<Categoria> categorias){
		if (categorias.isEmpty()){
			System.out.println("No hay categorias para mostrar");
			return;
		}

		System.out.println("Lista de categorias");
		System.out.printf("%-5s %-30s\n", "ID", "Nombre");
		System.out.println("-----------------------------------");

		for (Categoria categoria : categorias){
			System.out.printf("%-5d %-30s\n",
					categoria.getId(),
					categoria.getDenominacion());
		}
	}

	private static void listarArticulosManufacturados(List<ArticuloManufacturado> articuloManufacturados){
		if (articuloManufacturados.isEmpty()){
			System.out.println("No hay articulos manufacturados para mostrar");
			return;
		}

		System.out.println("Lista de articulos manufacturados");
		System.out.printf("%-5s %-30s %-15s %-20s\n", "ID", "Nombre", "Precio", "Categoría");
		System.out.println("-----------------------------------------------------------------------");

		for (ArticuloManufacturado articuloManufacturado : articuloManufacturados){
			System.out.printf("%-5d %-30s $%-15.2f %-20s\n",
					articuloManufacturado.getId(),
					articuloManufacturado.getDenominacion(),
					articuloManufacturado.getPrecioVenta(),
					articuloManufacturado.getCategoria() != null ? articuloManufacturado.getCategoria().getDenominacion() : "Sin categoría");
		}
	}

	private static void crearArticuloManufacturado(Scanner scanner,
												   ArticuloInsumoService articuloInsumoService,
												   ArticuloManufacturadoService articuloManufacturadoService,
												   List<Categoria> categorias){
		System.out.println("Crear articulo manufacturado");

		ArticuloManufacturado articuloManufacturado = new ArticuloManufacturado();

		System.out.println("Nombre del articulo manufacturado: ");
		articuloManufacturado.setDenominacion(scanner.nextLine());

		System.out.println("Descripcion:");
		articuloManufacturado.setDescripcion(scanner.nextLine());

		System.out.println("Precio de venta: ");
		try {
			articuloManufacturado.setPrecioVenta(Double.parseDouble(scanner.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("El precio debe ser un numero valido");
			return;
		}

		System.out.println("Tiempo estimado de preparacion (minutos): ");
		try {
			articuloManufacturado.setTiempoEstimadoMinutos(Integer.parseInt(scanner.nextLine()));
		} catch (NumberFormatException e) {
			System.out.println("El tiempo debe ser un numero entero");
			return;
		}

		System.out.println("Preparacion (instrucciones): ");
		articuloManufacturado.setPreparacion(scanner.nextLine());

		//seleciono la categoria del articulo
		System.out.println("Seleccione una categoria para el articulo: ");
		listarCategorias(categorias);
		System.out.println("Ingrese el ID de la categoria a elegir: ");

		try {
			int idCategoria = Integer.parseInt(scanner.nextLine());
			boolean categoriaEncontrada = false;

			for (Categoria categoria : categorias){
				if (categoria.getId() == idCategoria){
					articuloManufacturado.setCategoria(categoria);
					categoriaEncontrada = true;
					break;
				}
			}

			if (!categoriaEncontrada){
				System.out.println("Categoria no encontrada, el articulo se creara sin categoria");
			}
		} catch (NumberFormatException e){
			System.out.println("ID no valido, el articulo se creara sin categoria");
		}

		//agrego los ingredientes
		List<ArticuloManufacturadoDetalle> detalles = new ArrayList<>();
		boolean agregarMasIngredientes = true;

		//mapeo para llevar el stock utilizado pero no guardado todavia en la BD
		Map<Long, Integer> stockComprometido = new HashMap<>();

		while (agregarMasIngredientes){
			//muestro los insumos disponibles
			List<ArticuloInsumo> insumos = articuloInsumoService.findAll();

			System.out.println("\nLista de insumos");
			System.out.printf("%-5s %-30s %-10s %-15s %-15s\n", "ID", "Nombre", "Stock", "Unidad", "Precio Venta");
			System.out.println("-----------------------------------------------------------------------");

			for (ArticuloInsumo insumo : insumos) {
				//stock actual menos usado para elaborar producto
				int stockDisponible = insumo.getStockActual();
				if (stockComprometido.containsKey(insumo.getId())) {
					stockDisponible -= stockComprometido.get(insumo.getId());
				}

				System.out.printf("%-5d %-30s %-10d %-15s $%-15.2f\n",
						insumo.getId(),
						insumo.getDenominacion(),
						stockDisponible,  //stock disponible
						insumo.getUnidadMedida().getDenominacion(),
						insumo.getPrecioVenta());
			}

			System.out.println("Ingrese el ID del ingrediente a usar (0 para salir): ");
			int insumoId;
			try{
				insumoId = Integer.parseInt(scanner.nextLine());
				if (insumoId == 0){
					agregarMasIngredientes = false;
					continue;
				}
			} catch (NumberFormatException e) {
				System.out.println("Numero invalido, ingrese nuevamente un numero");
				continue;
			}

			//busco insumo por ID
			ArticuloInsumo insumo = null;
			for (ArticuloInsumo i : insumos){
				if (i.getId() == insumoId){
					insumo = i;
					break;
				}
			}

			if (insumo == null){
				System.out.println("No se encontro el insumo, intente nuevamente");
				continue;
			}

			//calculo de stock disponible
			int stockDisponible = insumo.getStockActual();
			if (stockComprometido.containsKey(insumo.getId())) {
				stockDisponible -= stockComprometido.get(insumo.getId());
			}

			System.out.println("Cantidad de " + insumo.getDenominacion() + " (" + insumo.getUnidadMedida()
					.getDenominacion() + "): ");
			int cantidad;
			try {
				cantidad = Integer.parseInt(scanner.nextLine());
				if (cantidad <= 0){
					System.out.println("La cantidad debe ser mayor a 0");
					continue;
				}

				if (cantidad > insumo.getStockActual()){
					System.out.println("No hay suficiente stock disponible");
					System.out.println("Stock actual: " + insumo.getStockActual());
					continue;
				}
			}catch (NumberFormatException e){
				System.out.println("Ingrese un numero valido");
				continue;
			}

			//actualiza stock usado
			stockComprometido.put(insumo.getId(),
					stockComprometido.getOrDefault(insumo.getId(), 0) + cantidad);

			//creo el detalle del articulo
			ArticuloManufacturadoDetalle detalle = new ArticuloManufacturadoDetalle();
			detalle.setArticuloInsumo(insumo);
			detalle.setCantidad(cantidad);
			detalle.setArticuloManufacturado(articuloManufacturado);
			detalles.add(detalle);

			System.out.println("Agregar otro ingrediente? (s/n): ");
			String respuesta = scanner.nextLine();
			agregarMasIngredientes = respuesta.equalsIgnoreCase("s");
		}

		if (detalles.isEmpty()){
			System.out.println("No se puede crear un articulo sin ingredientes");
			return;
		}

		articuloManufacturado.setDetalles(detalles);

		//guardo articulo y actualizo stock
		try {
			ArticuloManufacturado articuloGuardado = articuloManufacturadoService
					.crearArticuloManufacturado(articuloManufacturado);
			System.out.println("Articulo creado con ID: " + articuloGuardado.getId());

			//detalles del articulo guardado
			System.out.println("Detalles del articulo: ");
			System.out.println("Nombre: " + articuloGuardado.getDenominacion());
			System.out.println("Descripcion: " + articuloGuardado.getDescripcion());
			System.out.println("Precio: $" + articuloGuardado.getPrecioVenta());
			System.out.println("Categoria: " + articuloGuardado.getCategoria() != null ? articuloGuardado
					.getCategoria().getDenominacion() : "Sin categoria");
			System.out.println("Tiempo de preparacion: " + articuloGuardado.getTiempoEstimadoMinutos() + " minutos");
			System.out.println("Preparacion: " + articuloGuardado.getPreparacion());
			System.out.println("Ingredientes: ");
			for (ArticuloManufacturadoDetalle detalle : articuloGuardado.getDetalles()){
				System.out.println("- " + detalle.getCantidad() + " " +
						detalle.getArticuloInsumo().getUnidadMedida().getDenominacion() + " de " +
						detalle.getArticuloInsumo().getDenominacion());
			}

			//stock actualizado
			System.out.println("Stock actualizado: ");
			List<ArticuloInsumo> insumosActualizados = articuloInsumoService.findAll();
			System.out.println("\nStock actualizado:");
			listarInsumos(insumosActualizados);
			//listarInsumos(articuloInsumoService.findAll());
		} catch (Exception e){
			System.out.println("Error al cargar el articulo: " + e.getMessage() );
		}
	}

	private void verDetallesArticuloManufacturado(Scanner scanner, ArticuloManufacturadoService articuloManufacturadoService) {
		List<ArticuloManufacturado> articulos = articuloManufacturadoService.findAll();

		if (articulos.isEmpty()){
			System.out.println("No hay articulos manufacturados registrados");
		}

		//lista de articulos
		System.out.println("\n----- LISTA DE ARTÍCULOS MANUFACTURADOS -----");
		System.out.printf("%-5s %-30s %-15s %-20s\n", "ID", "Nombre", "Precio", "Categoría");
		System.out.println("-----------------------------------------------------------------------");

		for (ArticuloManufacturado articulo : articulos){
			System.out.printf("%-5d %-30s $%-15.2f %-20s\n",
					articulo.getId(),
					articulo.getDenominacion(),
					articulo.getPrecioVenta(),
					articulo.getCategoria() != null ? articulo.getCategoria().getDenominacion() : "Sin categoría");
		}

		//seleccion de articulo
		System.out.print("\nIngrese el ID del artículo que desea ver en detalle (0 para cancelar): ");
		try {
			int articuloId = Integer.parseInt(scanner.nextLine());

			if (articuloId == 0){
				return;
			}

			//busqueda de articulo
			ArticuloManufacturado articuloSeleccionado = null;
			for (ArticuloManufacturado a : articulos){
				if (a.getId() == articuloId){
					articuloSeleccionado = a;
					break;
				}
			}

			if (articuloSeleccionado == null){
				System.out.println("Articulo manufacturado no encontrado");
				return;
			}

			//detalles
			System.out.println("\n========== DETALLES DEL ARTICULO ==========");
			System.out.println("ID: " + articuloSeleccionado.getId());
			System.out.println("Nombre: " + articuloSeleccionado.getDenominacion());
			System.out.println("Descripcion: " + articuloSeleccionado.getDescripcion());
			System.out.println("Precio de venta: $" + articuloSeleccionado.getPrecioVenta());
			System.out.println("Categoria: " + (articuloSeleccionado.getCategoria() != null ?
					articuloSeleccionado.getCategoria().getDenominacion() : "Sin categoria"));
			System.out.println("Tiempo de preparacion: " + articuloSeleccionado.getTiempoEstimadoMinutos() + " minutos");
			System.out.println("\nPreparación:");
			System.out.println(articuloSeleccionado.getPreparacion());

			//ingredientes
			System.out.println("\nIngredientes:");
			if (articuloSeleccionado.getDetalles() == null || articuloSeleccionado.getDetalles().isEmpty()) {
				System.out.println("No hay informacion de ingredientes disponible.");
			} else {
				System.out.printf("%-30s %-10s %-15s\n",
						"Ingrediente", "Cantidad", "Unidad");
				System.out.println("------------------------------------------------------------------------");

				double costoTotal = 0.0;
				for (ArticuloManufacturadoDetalle detalle : articuloSeleccionado.getDetalles()) {
					ArticuloInsumo insumo = detalle.getArticuloInsumo();
					int cantidad = detalle.getCantidad();

					System.out.printf("%-30s %-10d %-15s\n",
							insumo.getDenominacion(),
							cantidad,
							insumo.getUnidadMedida().getDenominacion());
				}
			}

			System.out.println("\nPresione Enter para continuar...");
			scanner.nextLine();
		} catch (NumberFormatException e) {
			System.out.println("Por favor, ingrese un numero valido.");
		} catch (Exception e) {
			System.out.println("Error al obtener detalles: " + e.getMessage());
		}
	}

	private static void eliminarInsumo(Scanner scanner, ArticuloInsumoService articuloInsumoService) {
		System.out.println("\n----- ELIMINAR INSUMO -----");

		//lista insumos disponibles
		List<ArticuloInsumo> insumos = articuloInsumoService.findAll();
		if (insumos.isEmpty()) {
			System.out.println("No hay insumos registrados para eliminar.");
			return;
		}

		listarInsumos(insumos);

		System.out.print("\nIngrese el ID del insumo a eliminar (0 para cancelar): ");
		try {
			Long insumoId = Long.parseLong(scanner.nextLine());

			if (insumoId == 0) {
				System.out.println("Operación cancelada.");
				return;
			}

			//verifica que el insumo exista
			ArticuloInsumo insumoAEliminar = null;
			for (ArticuloInsumo insumo : insumos) {
				if (insumo.getId().equals(insumoId)) {
					insumoAEliminar = insumo;
					break;
				}
			}

			if (insumoAEliminar == null) {
				System.out.println("Error: Insumo no encontrado.");
				return;
			}

			//confirmar eliminación
			System.out.print("¿Está seguro de eliminar el insumo '" +
					insumoAEliminar.getDenominacion() + "'? (s/n): ");
			String confirmar = scanner.nextLine();

			if (confirmar.equalsIgnoreCase("s")) {
				try {
					articuloInsumoService.deleteById(insumoId);
					System.out.println("¡Insumo eliminado correctamente!");
				} catch (Exception e) {
					System.out.println("Error al eliminar el insumo: " + e.getMessage());

					// Más información sobre el error
					e.printStackTrace();
				}
			} else {
				System.out.println("Operación cancelada.");
			}
		} catch (NumberFormatException e) {
			System.out.println("Por favor, ingrese un ID válido.");
		}
	}

	private static void eliminarCategoria(Scanner scanner, CategoriaRepository categoriaRepository) {
		System.out.println("\n----- ELIMINAR CATEGORÍA -----");

		//lista categorías disponibles
		List<Categoria> categorias = categoriaRepository.findAll();
		if (categorias.isEmpty()) {
			System.out.println("No hay categorías registradas para eliminar.");
			return;
		}

		listarCategorias(categorias);

		System.out.print("\nIngrese el ID de la categoría a eliminar (0 para cancelar): ");
		try {
			int categoriaId = Integer.parseInt(scanner.nextLine());

			if (categoriaId == 0) {
				System.out.println("Operación cancelada.");
				return;
			}

			//verifica que la categoría exista
			Categoria categoriaAEliminar = null;
			for (Categoria categoria : categorias) {
				if (categoria.getId() == categoriaId) {
					categoriaAEliminar = categoria;
					break;
				}
			}

			if (categoriaAEliminar == null) {
				System.out.println("Error: Categoría no encontrada.");
				return;
			}

			//confirmar eliminación
			System.out.print("¿Está seguro de eliminar la categoría '" +
					categoriaAEliminar.getDenominacion() + "'? (s/n): ");
			String confirmar = scanner.nextLine();

			if (confirmar.equalsIgnoreCase("s")) {
				//verifica si la categoría está siendo usada en algún artículo manufacturado
				try {
					categoriaRepository.deleteById((long) categoriaId);
					System.out.println("¡Categoría eliminada correctamente!");
				} catch (Exception e) {
					System.out.println("Error al eliminar la categoría: " + e.getMessage());
					System.out.println("Es posible que esta categoría esté siendo utilizada en algún artículo manufacturado.");
				}
			} else {
				System.out.println("Operación cancelada.");
			}

		} catch (NumberFormatException e) {
			System.out.println("Por favor, ingrese un ID válido.");
		}
	}

	private static void eliminarArticuloManufacturado(Scanner scanner, ArticuloManufacturadoService articuloManufacturadoService) {
		System.out.println("\n----- ELIMINAR ARTÍCULO MANUFACTURADO -----");

		//listar artículos manufacturados disponibles
		List<ArticuloManufacturado> articulos = articuloManufacturadoService.findAll();
		if (articulos.isEmpty()) {
			System.out.println("No hay artículos manufacturados registrados para eliminar.");
			return;
		}

		listarArticulosManufacturados(articulos);

		System.out.print("\nIngrese el ID del artículo a eliminar (0 para cancelar): ");
		try {
			int articuloId = Integer.parseInt(scanner.nextLine());

			if (articuloId == 0) {
				System.out.println("Operación cancelada.");
				return;
			}

			//verifica que el artículo exista
			ArticuloManufacturado articuloAEliminar = null;
			for (ArticuloManufacturado articulo : articulos) {
				if (articulo.getId() == articuloId) {
					articuloAEliminar = articulo;
					break;
				}
			}

			if (articuloAEliminar == null) {
				System.out.println("Error: Artículo no encontrado.");
				return;
			}

			//confirma eliminación
			System.out.print("¿Está seguro de eliminar el artículo '" +
					articuloAEliminar.getDenominacion() + "'? (s/n): ");
			String confirmar = scanner.nextLine();

			if (confirmar.equalsIgnoreCase("s")) {
				try {
					articuloManufacturadoService.deleteById((long) articuloId);
					System.out.println("¡Artículo manufacturado eliminado correctamente!");
				} catch (Exception e) {
					System.out.println("Error al eliminar el artículo: " + e.getMessage());
					System.out.println("Es posible que este artículo esté siendo utilizado en algún pedido.");
				}
			} else {
				System.out.println("Operación cancelada.");
			}

		} catch (NumberFormatException e) {
			System.out.println("Por favor, ingrese un ID válido.");
		}
	}
	/*
	* lista de detalles del producto realizado - hecho
	* tabla unidad de medidas me crea cada vez que corro el programa las 3 unidadeds ingresadas al principio - hecho?a revisar
	* eliminar un producto
	* eliminar un insumo
	* eliminar categoria
	* crear subcategorias
	* */
}
