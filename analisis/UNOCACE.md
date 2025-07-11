Diagnóstico técnico para la implementación de INATrace 

El presente informe tiene como objetivo emitir los resultados de la evaluación de la infraestructura tecnológica y otros aspectos clave, para identificar fortalezas, debilidades y necesidades de cara a la implementación del sistema digital de trazabilidad INAtrace.

Nombre de la organización: UNOCACE
Ubicación: Km 30 vía Duran - Boliche - Milagro, Recinto El Deseo - Ecuador
Personas entrevistadas: Kevin Alvarez, Lenin Robayo
Fecha de visita: 23.06.2025
Entrevistadores: Wilder Bravo, Raúl Guerrero


1.	Infraestructura tecnológica 
-	Cuentan con un servidor local HPE ProLiant ML350 Gen10, ubicado en las instalaciones de la organización y que se usa para alojar el sistema contable, actualmente se encuentran cotizando un nuevo servidor con mejores capacidades. 
-	Tienen un equipo NAS para almacenamiento de archivos en la red de la organización, con una capacidad de 32 TB, el cual usa trabaja en modo espejo. 
-	Se cuenta con un esquema de respaldo de información que se ejecuta periódicamente en el equipo NAS. 
-	Cuenta con un esquema adicional de respaldo de información diario en Dropbox para otros datos.
-	No se cuenta con firewall hardware para asegurar la protección de la red.
-	Cuenta con UPS de respaldo energético con capacidad para 10 horas, su capacidad de carga al 100% demora un aproximado de 24 horas.
-	No cuenta con un cuarto frío para los equipos.

2.	Equipos de red
-	Cuentan con cobertura de red LAN, física e inalámbrica en las instalaciones, con velocidad promedio de 250 Mbps.
-	No se tiene cobertura de red WAN.
-	No cuentan con IP pública.
-	Cuentan con equipos que permiten distribuir la señal de internet localmente (Access Point, Routers, Cableado estructurado).
-	Cuentan con equipos electrónicos como laptops, impresoras, tablets.  
-	Existe un proceso de cotización de 10 tablets con mejores prestaciones.
-	Cuentan con un solo enlace de internet, no cuentan con uno de respaldo.

3.	Sistemas de información y aplicaciones
Cuenta con los siguientes sistemas y aplicaciones internas:
-	Sistema contable que contratan a través de un proveedor externo.
-	KoboToolBox para captura de información en campo y luego es revisado en planta central.
-	Power BI para analizar la información que se recolecta a través de KoboToolBox.
-	Paquete de ofimática para labores diarias, especialmente Excel como herramienta clave en sus procesos.
-	Tienen una landing page informativa que es parte de un servicio externo contratado
-	Adicionalmente hacen uso del sistema GUIA de Agrocalidad, QGis, Python y en ocasiones Laravel (PHP) para temas puntuales.
-	Se usa un Drive de Google para el almacenamiento y trabajo de documentos de forma colaborativa para cuando se necesita fuera de planta central.

4.	Capacidades humanas y organizativas
-	Cuentan con un recurso humano dedicado a soporte tecnológico y aseguramiento de la infraestructura tecnológica.
-	Cuenta con un recurso humano dedicado para temas de trazabilidad y validación de información de los centros de acopio recibida vía e-mail.
-	El personal técnico de TI no tiene un rol específico en temas relacionados con desarrollo de software.
-	Se estima una calificación del nivel de capacidades digitales del personal en básico – intermedio.
-	El nivel de uso de paquetes de ofimática se considera un nivel intermedio.
-	Se estima que la digitalización de los procesos se encuentra en un nivel intermedio.
-	Las decisiones relacionadas con temas tecnológicos son definidos por el personal de TI y socializados al gerente general. 

5.	Gestión de Datos y flujos de información
-	Cuentan con una leve estructura sobre las consideraciones legales de protección de datos personales y consentimiento informado.
-	No cuentan con una política sobre gestión y/o gobernanza de datos.
-	Existe el reto de manejar diferentes fuentes de datos en diferentes formatos, lo cual dificulta la estandarización de la información.
-	Se realiza registros diarios de información para los procesos operativos, con respecto al SIC se hace 2 veces al año.
-	El navegador más utilizado es Google Chrome.
-	Se tiene noción funcional sobre la necesidad de hacer que los sistemas interoperen e intercambien datos, por ejemplo: INATrace y la ficha que se realiza en KoboTollBox para inhabilitar los usuarios que no cumplan con las condiciones establecidas.
-	Se considera como punto crítico del proceso de trazabilidad al centro de acopio, el cual se puede minimizar con un fortalecimiento de capacidades en el uso de INATrace.

6.	Conclusiones 
-	Si se desea disponer un servicio hacia la web desde las instalaciones de UNOCACE se debe realizar una mejora sustancial en su infraestructura tecnológica (Lo subrayado es prioritario):

o	Adquisición de un servidor de mejores prestaciones
o	Mejora del sistema de enfriamiento
o	Cuarto frío exclusivo para los equipos
o	Adquisición de enlace de salida que permita contar con una IP pública
o	Mejora en el sistema de respaldo de energía 

-	El alojamiento en una nube privada a largo plazo es una opción importante, debido al aseguramiento de infraestructura de rápido acceso y de fácil escalamiento.
-	El uso de infraestructura on-premise (en las instalaciones) requiere de mayor inversión y una carga operativa adicional para el personal de TI.
-	El uso de infraestructura en la nube le da al personal de TI la capacidad de administrar solo los recursos otorgados por el proveedor a nivel de software y más no el funcionamiento de la infraestructura a nivel de hardware.
-	Los modelos híbridos (combinación de nube y on-premise) ofrecen flexibilidad para migrar gradualmente cargas de trabajo críticas, permitiendo optimizar costos, cumplir con normativas locales y garantizar mayor control en procesos sensibles.
-	La escalabilidad bajo demanda que ofrece la nube facilita la adaptación ante picos de uso o crecimiento organizacional, lo que la convierte en una opción estratégica para proyectos en expansión o entornos de alta variabilidad operativa.
-	Si se considera la adquisición de alojamiento en la nube se debe plantear un proceso sostenible a largo plazo, 3-4 años.

7.	Recomendaciones
-	Considerar una solución en una nube privada para la implementación inicial de INATrace, aprovechando la escalabilidad, la alta disponibilidad y el bajo tiempo de despliegue que ofrecen estos entornos. Esto permitirá iniciar el uso del sistema sin depender de una infraestructura física compleja o costosa, reduciendo el tiempo de entrada en operación.
-	Diseñar una estrategia de infraestructura tecnológica orientada a la sostenibilidad del sistema, contemplando no solo el despliegue técnico, sino también el soporte a largo plazo a través de la capacitación del personal TI, para la seguridad y actualización del sistema. Esta visión integral garantizará que INATrace no solo funcione bien al inicio, sino que evolucione de forma estable dentro de la organización.
 
8.	Anexos

