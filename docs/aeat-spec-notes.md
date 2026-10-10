# Verifactu spec (investigación 2026-10-10)
Texto completo extraído de los 3 PDF oficiales en ./verifactu-pdf/{huella,qr,sweb}.txt (pdftotext/pypdf).
Fuentes índice: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/informacion-tecnica.html (actualizada 07/10/2026)
- Huella: https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/Veri-Factu_especificaciones_huella_hash_registros.pdf
- QR: https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/DetalleEspecificacTecnCodigoQRfactura.pdf
- Descripción servicio web (101 pág.): https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/Veri-Factu_Descripcion_SWeb.pdf
- WSDL: https://www.agenciatributaria.es/AEAT.desarrolladores/Desarrolladores/_menu_/Documentacion/Sistemas_Informaticos_de_Facturacion_y_Sistemas_VERI_FACTU/WSDL_de_los_servicios_web/WSDL_de_los_servicios_web.html
- Validaciones y errores: https://www.agenciatributaria.es/AEAT.desarrolladores/Desarrolladores/_menu_/Documentacion/Sistemas_Informaticos_de_Facturacion_y_Sistemas_VERI_FACTU/Validaciones_y_errores/Validaciones_y_errores.html
- Firma: https://www.agenciatributaria.es/AEAT.desarrolladores/Desarrolladores/_menu_/Documentacion/Sistemas_Informaticos_de_Facturacion_y_Sistemas_VERI_FACTU/Especificaciones_tecnicas_para_generacion_de_la_firma_electronica_de_los_registros_de_facturacion/Especificaciones_tecnicas_para_generacion_de_la_firma_electronica_de_los_registros_de_facturacion.html
- FAQ desarrolladores (04-12-2025): https://sede.agenciatributaria.gob.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/FAQs-Desarrolladores.pdf
- FAQ huella (SHA-256): https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/huella-hash.html
- Portal pruebas: https://preportal.aeat.es
- RD 1007/2023 consolidado (DF4ª, modif. por RD 254/2025 y RDL 15/2025): https://www.boe.es/buscar/act.php?id=BOE-A-2023-24840
## Extractos literales
### Huella (PDF v0.1.2, 27/08/2024) — pág. 7-13
valor a continuación), como en los siguientes ejemplos:
Aplicación: VERI*FACTU Versión: 0.1.2
VERI*FACTU Página: 7/13
 Primer registro de facturación, donde no hay una huella “anterior”:
…ImporteTotal=123.45&Huella=&FechaHoraHusoGenRegistro=…
 Se informa NIF, pero no ID, en un registro de evento, al ser obligatorio
informar, de forma excluyente, uno u otro:
NIF=89890001K&ID=&IdSistemaInformatico=…
Dicha cadena de caracteres será codificada en un array de bytes en formato
UTF-8 para generar la entrada del algoritmo (o función) de huella o «hash».
Aplicación: VERI*FACTU Versión: 0.1.2
VERI*FACTU Página: 8/13
4. Ejemplo tratamiento de datos de entrada en
lenguaje Java
Se describe un ejemplo de tratamiento de datos de entrada para un registro de
facturación de alta en lenguaje de programación Java.
￼
Aplicación: VERI*FACTU Versión: 0.1.2
VERI*FACTU Página: 9/13
5. Datos de salida
Se aplicará el algoritmo de huella o «hash» a la cadena generada a partir de los
datos anteriormente indicados del XML del registro de que se trate.
El formato de salida será:
 En sistema hexadecimal.
 En mayúsculas.
El tamaño será de 64 caracteres alfanuméricos.
El resultado obtenido de dicho algoritmo de huella o «hash» se informará en el
siguiente campo del registro de facturación o de evento, dependiendo del tipo de
registro que se esté generando:
a) Campo donde se informará el valor de la huella o «hash» en los registros
de facturación de alta (y la “ruta” de su localización dentro del registro):
 Huella (RegistroAlta/Huella)
b) Campo donde se informará el valor de la huella o «hash» en los registros
de facturación de anulación (y la “ruta” de su localización dentro del
registro):
 Huella (RegistroAnulacion/Huella)
c) Campo donde se informará el valor de la huella o «hash» en los registros
de evento (y la “ruta” de su localización dentro del registro):
 HuellaEvento (RegistroEvento/Evento/HuellaEvento)
El dato de la huella –así calculada– del registro al que se refiera siempre ha de
ir informado en el correspondiente campo de dicho registro. Es decir, incluso en
el caso de que sea el primer registro de facturación o de evento (en cuyo caso
se deberá indicar en el campo “PrimerRegistro” o “PrimerEvento” el valor a “S”
y no será necesario informar los campos de los bloques “RegistroAnterior” o
“EventoAnterior”, respectivamente), también será necesario generar el contenido
de los campos Huella o HuellaEvento detallados anteriormente e incluirlos en
el correspondiente registro. En el punto 6. Ejemplos de datos de salida, se
adjunta esta casuística.
6.1. Caso 1: primer registro de facturación –en este caso, de
alta– en un Sistema Informático de Facturación (SIF)
Se parte de los siguientes datos de entrada en el XML:
1. IDEmisorFactura: 89890001K
2. NumSerieFactura: 12345678/G33
3. FechaExpedicionFactura: 01-01-2024
4. TipoFactura: F1
5. CuotaTotal: 12.35
6. ImporteTotal: 123.45
7. Huella (*):
8. FechaHoraHusoGenRegistro: 2024-01-01T19:20:30+01:00
(*) Sin contenido, al tratarse del primer registro de ese SIF y, por tanto, no haber
registro de facturación anterior.
Se obtendría la siguiente cadena de caracteres sobre la que aplicar el algoritmo
de huella o «hash»:
IDEmisorFactura=89890001K&NumSerieFactura=12345678/G33&FechaExped
icionFactura=01-01-
2024&TipoFactura=F1&CuotaTotal=12.35&ImporteTotal=123.45&Huella=&Fec
haHoraHusoGenRegistro=2024-01-01T19:20:30+01:00
Aplicando el algoritmo de huella o «hash» para los siguientes datos de entrada:
calcularHuella("IDEmisorFactura=89890001K&NumSerieFactura=12345678/G3
3&FechaExpedicionFactura=01-01-
2024&TipoFactura=F1&CuotaTotal=12.35&ImporteTotal=123.45&Huella=&Fec
haHoraHusoGenRegistro=2024-01-01T19:20:30+01:00”);
Debería devolver el siguiente dato de salida como valor de la huella o «hash»:
3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F3
8F12F60
Aplicación: VERI*FACTU Versión: 0.1.2
VERI*FACTU Página: 11/13
6.2. Caso 2: registro de facturación –en este caso de alta–
con registro de facturación anterior existente en el SIF
(siendo, por tanto, el segundo registro o sucesivo)
Partiendo de los siguientes datos de entrada en el XML:
1. IDEmisorFactura: 89890001K
2. NumSerieFactura: 12345679/G34
3. FechaExpedicionFactura: 01-01-2024
4. TipoFactura: F1
5. CuotaTotal: 12.35
6. ImporteTotal: 123.45
7. Huella:
3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058
073F38F12F60
8. FechaHoraHusoGenRegistro: 2024-01-01T19:20:35+01:00
Se obtendría la siguiente cadena de caracteres sobre la que aplicar el algoritmo
de huella o «hash»:
IDEmisorFactura=89890001K&NumSerieFactura=12345679/G34&FechaExped
icionFactura=01-01-
2024&TipoFactura=F1&CuotaTotal=12.35&ImporteTotal=123.45&Huella=3C46
4DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12
F60&FechaHoraHusoGenRegistro=2024-01-01T19:20:35+01:00
Aplicando el algoritmo de huella o «hash» para los siguientes datos de entrada:
calcularHuella("IDEmisorFactura=89890001K&NumSerieFactura=12345679/G3
4&FechaExpedicionFactura=01-01-
2024&TipoFactura=F1&CuotaTotal=12.35&ImporteTotal=123.45&Huella=3C46
4DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F
60&FechaHoraHusoGenRegistro=2024-01-01T19:20:35+01:00”);
Debería devolver el siguiente dato de salida como valor de la huella o «hash»:
F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF6ACB8935802
A2B97
Aplicación: VERI*FACTU Versión: 0.1.2
VERI*FACTU Página: 12/13
6.3. Caso 3: registro de facturación –en este caso, de
anulación– con registro de facturación anterior
existente en el SIF (siendo, por tanto, el segundo
registro o sucesivo)
Partiendo de los siguientes datos de entrada en el XML:
1. IDEmisorFacturaAnulada: 89890001K
2. NumSerieFacturaAnulada: 12345679/G34
3. FechaExpedicionFacturaAnulada: 01-01-2024
4. Huella:
F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF6ACB8
935802A2B97
5. FechaHoraHusoGenRegistro: 2024-01-01T19:20:40+01:00
Se obtendría la siguiente cadena de caracteres sobre la que aplicar el algoritmo
de huella o «hash»:
IDEmisorFacturaAnulada=89890001K&NumSerieFacturaAnulada=12345679/G
34&FechaExpedicionFacturaAnulada=01-01-
2024&Huella=F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF
6ACB8935802A2B97&FechaHoraHusoGenRegistro=2024-01-
01T19:20:40+01:00
Aplicando el algoritmo de huella o «hash» para los siguientes datos de entrada:
calcularHuella(“IDEmisorFacturaAnulada=89890001K&NumSerieFacturaAnula
da=12345679/G34&FechaExpedicionFacturaAnulada=01-01-
2024&Huella=F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF
6ACB8935802A2B97&FechaHoraHusoGenRegistro=2024-01-
01T19:20:40+01:00”);
Debería devolver el siguiente dato de salida como valor de la huella o «hash»:
177547C0D57AC74748561D054A9CEC14B4C4EA23D1BEFD6F2E69E3A388
F90C68
Aplicación: VERI*FACTU Versión: 0.1.2
VERI*FACTU Página: 13/13
7. Validación
El sistema informático de facturación deberá asegurar que la huella o «hash»
generada sea acorde a las especificaciones y formato detallados anteriormente
en la generación de la información de los registros de facturación y, en su caso,
### QR (PDF v0.5.0, 10/12/2025) — pág. 6-9
la misma en soporte digital, incluirá los siguientes elementos que, cumpliendo con los
requisitos que se determinen, deberán ser legibles y estar impresos con una resolución
apropiada:
a) Un código «QR», que deberá cumplir con las especificaciones del artículo 21.
b) En caso de facturas expedidas por «Sistemas de emisión de facturas verificables»
o «VERI*FACTU», según los artículos 15 y 16 del Reglamento, la frase «Factura
verificable en la sede electrónica de la AEAT» o «VERI*FACTU», que deberá tener
un tipo de letra y tamaño bien visibles, similares a los del resto de datos de la
factura.
…
Artículo 21. Código «QR».
1. El código «QR» deberá tener un tamaño entre 30x30 y 40x40 milímetros y seguir las
especificaciones de la norma ISO/IEC 18004:2015. Para la generación del código «QR»
se empleará el nivel M (medio) de corrección de errores. En la sede electrónica de la
Agencia Estatal de Administración Tributaria se publicarán la ubicación y presentación
del mismo dentro de la factura, pudiéndose completar con otras características a cumplir.
2. El contenido del código «QR» será el siguiente:
a) «URL» del servicio de cotejo o remisión de información por parte del receptor de la
factura, del cual se informará en la sede electrónica de la Agencia Estatal de
Administración Tributaria.
b) Información de la factura que formará parte de la «URL»:
1.º NIF del obligado a expedir la factura.
2.º Número de serie y número de la factura expedida.
3.º Fecha de expedición de la factura.
4.º Importe total de la factura.
Tanto el formato detallado de esta «URL», que podrá ser distinto dependiendo de si el
sistema informático que expide la factura y genera su correspondiente código «QR» es
o no un sistema informático «VERI*FACTU», como la codificación y formato de la
información requerida se especificarán en el correspondiente documento técnico que
será publicado en la sede electrónica de la Agencia Estatal de Administración Tributaria.
Aplicación: «VERI*FACTU» Versión: 0.5.0
«VERI*FACTU» Página: 7/35
3. Detalles de la ubicación y presentación del código
«QR» dentro de la factura.
El código «QR» deberá cumplir con las especificaciones dadas en la orden, que se
completarán con los detalles de este documento.
El contraste de colores entre el código «QR» y el fondo debe ser lo suficientemente alto
para asegurar la legibilidad.
A este respecto, se deben mantener como mínimo 2 milímetros de espacio vacío (en
blanco) alrededor de los cuatro lados del código «QR», recomendándose que sean 6
milímetros.
El código «QR» se situará al principio de la factura, antes de que empiece el contenido
de ésta generado por el sistema informático de facturación, a menos que se justifique la
existencia de algún obstáculo para ello, en cuyo caso, deberá quedar siempre bien visible
y estar claramente separado y diferenciado –de forma que destaque– del resto de
contenidos y otros posibles «QR», ocupando un lugar preeminente. Así pues, en
principio, será siempre el primer código «QR» que aparecerá en la factura (en el caso de
que la factura contuviera otros códigos «QR» para otros cometidos). Si la factura ocupara
varias páginas, el código «QR» aparecería una única vez, en la primera página. Si se
utiliza un formato de orientación vertical para organizar y disponer el contenido de la
factura, el código «QR» se situará arriba de esta, próximo al margen superior,
preferiblemente centrado respecto a los márgenes izquierdo y derecho de la factura (o,
si no, hacia el margen izquierdo-superior). En el caso de utilizar un formato de orientación
horizontal (apaisado) para la factura, el código «QR» se situará a la izquierda de esta,
preferiblemente cercana al margen superior-izquierdo (o, si no, centrada respecto a los
márgenes superior e inferior de la factura).
La presentación del código «QR» incluirá también un texto que siempre deberá ir
precediéndolo: «QR tributario:», y que se situará encima del propio código «QR»
(preferiblemente centrado con respecto a este), de manera que sirva para identificarlo y
distinguirlo de otros posibles códigos «QR» que pudiera contener la factura para otros
cometidos.
Además, en el caso de facturas expedidas por sistemas que emiten facturas verificables,
justo debajo del código «QR» deberá aparecer la frase «Factura verificable en la sede
electrónica de la AEAT» o «VERI*FACTU», preferiblemente centrada con respecto al
código «QR». Si no cabe toda la frase en una sola línea, podrán utilizarse varias líneas
hasta completarla.
Tanto el texto que siempre debe preceder al código «QR», como, en su caso, la frase
que habrán de incluir los sistemas «VERI*FACTU» deberán tener un tipo de letra y
tamaño legibles, siempre iguales o superiores a los del resto de datos de la factura.
En el anexo de este documento se ofrecen algunos ejemplos posibles de códigos «QR»
en los varios escenarios que se pueden dar en función de diversos aspectos.
Aplicación: «VERI*FACTU» Versión: 0.5.0
«VERI*FACTU» Página: 8/35
4. Consideraciones previas sobre la «URL» del
código «QR».
A fin de asegurar que todos sus caracteres sean leídos e interpretados correctamente,
la «URL» de cotejo o remisión de información de la factura contenida en el código «QR»
que debe aparecer en la factura, concretamente el contenido de los parámetros, deberán
ser codificados de forma adecuada siguiendo los estándares generales de las
aplicaciones en entorno web («URL encoding») y utilizando la codificación UTF-8.
Con el fin de especificar el uso del «URL encoding», se muestra un ejemplo de factura
con el siguiente conjunto de parámetros, para el entorno de Pruebas Externas:
• URL base: https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR?
• Parámetro nif: 89890001K
• Parámetro numserie: 12345678&G33
• Parámetro fecha: 01-01-2024
• Parámetro importe: 241.4
El resultado de aplicar la codificación de los parámetros sería la siguiente:
https://prewww2.aeat.es/wlpl/TIKE-
CONT/ValidarQR?nif=89890001K&numserie=12345678%26G33&fecha=01-01-
2024&importe=241.4
A modo de ejemplo erróneo se adjunta la siguiente URL, donde no ha sido realizada la
codificación necesaria:
https://prewww2.aeat.es/wlpl/TIKE-
CONT/ValidarQR?nif=89890001K&numserie=12345678&G33&fecha=01-01-
2024&importe=241.4
En el siguiente apartado 4.1 se describe el proceso para el tratamiento de «URL
encoding» para generar «URL» de «QR válidas».
Por último, es importante destacar que las cadenas de texto solo pueden contener
caracteres ASCII con códigos del 32 al 126 (caracteres imprimibles).
Aplicación: «VERI*FACTU» Versión: 0.5.0
«VERI*FACTU» Página: 9/35
4.1. Ejemplo de tratamiento de «URL encoding» en lenguaje
java
Se describe un ejemplo de tratamiento de «URL encoding» para generar «URL» de
código «QR» válidas en lenguaje de programación Java.
￼
Aplicación: «VERI*FACTU» Versión: 0.5.0
«VERI*FACTU» Página: 10/35
5. Formato de la «URL» del código «QR».
Dicha «URL» tendrá el siguiente formato, dependiendo de si el sistema informático de
facturación emite facturas verificables o no, así como del entorno de ejecución al que
vaya dirigida:
5.1. Sistema que emite facturas verificables
• Entorno de pruebas (Portal de Pruebas Externas):
https://prewww2.aeat.es/wlpl/TIKE-
CONT/ValidarQR?nif=XXXXXXXXY&numserie=YYYY...YYYY&fecha=DD-MM-
AAAA&importe=NNNNNNNNN.DD
• Entorno de Producción:
https://www2.agenciatributaria.gob.es/wlpl/TIKE-
CONT/ValidarQR?nif=XXXXXXXXY&numserie=YYYY...YYYY&fecha=DD-MM-
AAAA&importe=NNNNNNNNN.DD
5.2. Sistema que emite facturas no verificables
• Entorno de pruebas (Portal de Pruebas Externas):
https://prewww2.aeat.es/wlpl/TIKE-
CONT/ValidarQRNoVerifactu?nif=XXXXXXXXX&numserie=YYYYYYYY&fecha=
DD-MM-AAAA&importe=NNNNNNNN.DD
• Entorno de Producción:
https://www2.agenciatributaria.gob.es/wlpl/TIKE-
CONT/ValidarQRNoVerifactu?nif=XXXXXXXXX&numserie=YYYYYYYY&fecha=
DD-MM-AAAA&importe=NNNNNNNN.DD
Aplicación: «VERI*FACTU» Versión: 0.5.0
«VERI*FACTU» Página: 11/35
6. Parámetros de la «URL» del código «QR».
La «URL» de cotejo o remisión de información de la factura contenida en el código «QR»
deberá incorporar únicamente los siguientes 4 parámetros obligatorios, acompañados
de sus correspondientes valores. El resultado será devuelto en formato “html” (web):
Parámetro Formato Longitud Obligatorio Descripción
nif Formato NIF 9 Sí NIF del obligado a expedir la
factura
numserie Cadena de
### Descripción SWeb (PDF v1.0.3, 28/07/2025) — esquemas, estados, TiempoEsperaEnvio
de alta como de anulación. Una vez enviado el mensaje XML, la AEAT procederá a
realizar automáticamente un proceso de validación, tanto a nivel de formato XML,
como de reglas de negocio.
Si el mensaje no supera alguna de las validaciones a nivel de formato XML, se
devolverá un mensaje de tipo «SoapFault», en el que se especificará el error concreto.
Si el mensaje supera las validaciones a nivel de formato XML, se procederá a realizar
las validaciones de negocio, devolviéndose un mensaje de respuesta con el resultado
de la validación y de su aceptación o no por la AEAT.
El número máximo de registros por envío es de 1.000.
La unidad de información, representada por un registro de facturación, podrá ser
aceptada, rechazada o aceptada con errores, consecuencia de las validaciones que se
realizan en el momento de la remisión.
Si los registros contuvieran errores, sólo se aceptarán aquellos registros para los que
no exista motivo de rechazo. En caso de rechazo, los obligados tributarios deberán
realizar las subsanaciones necesarias y proceder a una nueva remisión en la que
incluirán los registros que en su momento fueron rechazados.
El mensaje XML de respuesta enviado por la AEAT contendrá la relación de registros
aceptados y rechazados junto con la expresión del motivo por el que hayan sido
rechazados. En la respuesta también se informará del código seguro de verificación
(CSV) que servirá para dejar constancia de la remisión, excepto en el caso de que se
rechacen todos los registros enviados.
A su vez, en la respuesta también se incluye un resultado global de la remisión, que
puede ser aceptada (si no existen errores en ningún registro), aceptada parcialmente
(cuando existen registros aceptados y rechazados, o cuando existen registros
aceptados con errores admisibles) y rechazada (cuando todos los registros han sido
rechazados).
La etiqueta <IDFactura> contiene los campos que identifican de manera única a un
registro de facturación que son:
<NIF> + <NumSerieFactura> + <FechaExpedicionFactura>.
El formato del mensaje XML es el mismo tanto para una remisión voluntaria como para
la remisión a raíz de un requerimiento de la AEAT. Tener un esquema XSD único de
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 11/101
validación, facilitará la migración de sistemas que inicialmente emiten facturas no
verificables, a sistemas que emiten facturas verificables.
Aunque el esquema XSD es común, existirán URLs diferentes para la remisión
voluntaria y la remisión por requerimiento, pudiendo tener los servicios “ubicados” en
dichas URLs ciertas diferencias en las validaciones de negocio.
Los envíos a través de los servicios web devolverán un mensaje de respuesta en el
que se indicará tanto el resultado de la validación global del envío, como el resultado
específico de la validación de cada registro.
Para calificar el resultado global del envío se devolverá uno de los siguientes valores
como respuesta:
• Correcto (Aceptación completa)
• ParcialmenteCorrecto (Aceptación parcial)
• Incorrecto (Rechazo completo)
Para calificar el resultado de cada registro de facturación incluido en la remisión se
podrá devolver uno de los siguientes valores como respuesta:
• Correcto (Aceptado)
• AceptadoConErrores (Aceptado con errores)
• Incorrecto (Rechazado)
Descripción de los valores posibles de la respuesta global (o “estados
globales”) de una remisión.
Aceptación completa
/ anulación.
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 38/101
TiempoEsperaE
nvio
Segundos de espera entre envíos.
Para poder realizar el siguiente envío, el sistema
informático deberá esperar a que transcurran
<TiempoEsperaEnvio> segundos desde el anterior envío
o deberá esperar a tener acumulados un número de
registros de facturación igual al límite establecido en el
diseño de registro para cada envío, la circunstancia que
ocurra primero
Numérico
El número máximo de registros a remitir en cada envío queda determinado por el diseño de registro incluido en el apartado 2.2 del anexo.
El funcionamiento será el siguiente:
a) El sistema informático realiza el envío del primer conjunto de registros de facturación a la Agencia Estatal de Administración Tributaria.
b) La Agencia Estatal de Administración Tributaria devuelve, entre otros datos, un valor actualizado del parámetro de tiempo de espera «t»
entre envíos.
c) Para poder realizar el siguiente envío, el sistema informático deberá esperar a que transcurran «t» segundos desde el anterior envío o
deberá esperar a tener acumulados un número de registros de facturación igual al límite establecido en el diseño de registro para cada envío,
la circunstancia que ocurra primero.
d) El sistema informático realiza un nuevo envío cumpliendo con lo establecido en la letra c). En la respuesta puede recibir una nueva
actualización del valor del parámetro «t».
Ejemplo de respuesta con el parámetro de tiempo de espera de 60 segundos entre envíos:
<sf:TiempoEsperaEnvio>60</sf:TiempoEsperaEnvio>
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 41/101
10 Octubre
11 Noviembre
12 Diciembre
6.5.2. Respuesta de alta y anulación de registros de facturación.
L18→ Estado global del envío (respuesta al envío).
VALORES DESCRIPCIÓN
Correcto Todos los registros de facturación de la remisión tienen estado
“Correcto”.
ParcialmenteCorrecto Algunos registros de la remisión tienen estado “Incorrecto” o
“AceptadoConErrores”
.
Incorrecto Todos los registros de la remisión tienen estado “Incorrecto”.
L19→ Estado del envío del registro (respuesta al envío).
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 43/101
Campo <EstadoRegistro> de la respuesta al envío de alta y anulación. Especifica si el registro enviado se valida correctamente y es por tanto
admitido y registrado en el sistema.
VALORES DESCRIPCIÓN
Correcto El registro de facturación es totalmente correcto y se registra en el
sistema.
AceptadoConErrores El registro de facturación tiene errores que no provocan su rechazo.
Se registra en el sistema.
Incorrecto El registro de facturación tiene errores que provocan su rechazo. No
se registra en el sistema.
L20→ Código de error de registro.
La lista completa de errores se encuentra en el documento de validaciones.
L21→ Estado del registro de facturación de la factura (obtenido como respuesta cuando se rechaza el alta de un registro por duplicado).
Campo <EstadoRegistroDuplicado> de la respuesta a un alta cuando el registro se rechaza por duplicado. Especifica el estado del registro
almacenado previamente en el sistema.
VALORES DESCRIPCIÓN
Correcta El registro de facturación registrado previamente es correcto.
AceptadaConErrores El registro de facturación registrado previamente tiene algunos errores
Anulada El registro de facturación registrado previamente ha sido anulado
requerimiento. Por tanto, el esquema XSD es único para los dos casos.
Aunque el esquema XSD es común, existén URLs diferentes para la remisión voluntaria y ante requerimiento. Además, son distintos sistemas
de gestión en la AEAT, sin compartición de los registros de facturación remitidos entre ellos.
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 45/101
La definición de los servicios web para la remisión voluntaria y ante requerimiento se encuentra en el archivo WSDL en la siguiente dirección:
https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SistemaFacturacion.wsdl
￼
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 46/101
6.7. Tratamiento de cadenas de texto en campos XML
No se recomienda comenzar y/o finalizar con espacios en blanco las cadenas de texto presentes en los registros XML, en cualquier caso, se
eliminarán los espacios en blanco al inicio y al final de cada valor. Este proceso se reflejará en la respuesta de las peticiones y se almacenará
de esta manera en los sistemas de la AEAT.
Por ejemplo, si el campo NumSerieFactura contiene la siguiente información:
<NumSerieFactura> 12345678 / G33 </NumSerieFactura>
se almacenará y se responderá con el valor "12345678 / G33".
6.8. Valores permitidos en campos numéricos.
Para valores numéricos, los ceros por la izquierda no deberán emplearse (por ejemplo, 01 ó 001 ó 01230 serían incorrectos; en su lugar
debería ponerse 1, 1 y 1230 respectivamente). Tras el punto de separación decimal, los ceros por la derecha sólo podrán ser usados para
indicar la precisión decimal (por ejemplo: 12345.7 es lo mismo que 12345.70; y 12345 es lo mismo que 12345.0 y que 12345.00).
(Nota: dentro del formato fecha, los campos numéricos que expresen cada uno de los componentes de la misma sí deben llevar ceros por la
izquierda hasta completar el número de dígitos requerido, como, por ejemplo: 02-07-2014 (y no 2-7-2014).
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 47/101
6.9. Aclaración sobre escapado de caracteres especiales.
En caso de que fuera necesario consignar en un valor de un elemento XML algunos de los siguientes caracteres, se escaparán con las
entidades xml siguientes:
Carácter Carácter escapado
& &amp;
< &lt;
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 48/101
7. Anexo I: Definición de servicios y esquemas (entorno de PRUEBAS).
Contiene la definición de los servicios y esquemas de la versión 1.0.
7.1. Definición de servicios.
La definición de los servicios (WSDL) se encuentra en la siguiente dirección:
https://prewww2.aeat.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SistemaFacturacion.wsdl
7.2. Esquemas de Entrada
El esquema de los mensajes de entrada definidos se ha incluido en los siguientes archivos:
•
“SuministroInformacion.xsd”. Contiene la definición de tipos comunes:
https://prewww2.aeat.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SuministroInformacion.xsd
•
“SuministroLR.xsd”. Esquema de las operaciones (Alta y Anulación) establecidos:
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 49/101
https://prewww2.aeat.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SuministroLR.xsd
• ConsultaLR.xsd. Esquema de la operación de consulta.
https://prewww2.aeat.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/ConsultaLR.xsd
7.3. Esquemas de Salida.
El esquema de los mensajes de respuesta definidos se ha incluido en los siguientes archivos:
•
“RespuestaSuministro.xsd”. Esquema de respuesta de las operaciones (Alta y Anulación) establecidos:
https://prewww2.aeat.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/RespuestaSuministro.xsd
• RespuestaConsultaLR.xsd. Esquema de respuesta de las operaciones de consulta.
https://prewww2.aeat.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/RespuestaConsultaLR.xsd
Sistemas Informáticos de Facturación Versión: 1.0.3
Página: 50/101
