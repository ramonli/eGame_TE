package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.config.MLotteryContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class CastorHelper {
    private static Log logger = LogFactory.getLog(CastorHelper.class);
    private static MLotteryContext prop = MLotteryContext.getInstance();
    // All loading mapping should be cached to improve performance
    private static Map<String, XMLContext> mappings = new HashMap<String, XMLContext>(0);

    /**
     * Unmarshal a xml string into javabean.
     * 
     * @param input
     *            The orignial xml string.
     * @param mappingFile
     *            The mapping file path, a class path.
     * @return a javabean associated to xml input.
     */
    public static Object unmarshal(String input, String mappingFile) throws Exception {
        Unmarshaller u = getXmlContext(mappingFile).createUnmarshaller();
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        return u.unmarshal(new InputSource(bais));
    }

    /**
     * marshal a javabean into xml string.
     * 
     * @param input
     *            The input java bean
     * @param mappingFile
     *            The mapping file in class path
     * @return a xml representation of java bean.
     */
    public static String marshal(Object input, String mappingFile) throws Exception {
        /**
         * Make sure you are not using one of the static methods on the Marshaller/Unmarshaller. Any configuration
         * changes that you make to the Marshaller or Unmarshaller are not available from the static methods.
         */
        Marshaller m = getXmlContext(mappingFile).createMarshaller();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos));
        m.setWriter(writer);
        // Marshaller m = new Marshaller(new OutputStreamWriter(baos));
        // Anyone who wants to marshal Hibernate POJOs. This document shows how
        // prevent
        // undesirable XML output caused by Hibernate's lazy-loading technique.
        // As proxy objects usually implement proxy interfaces, Castor XML can
        // be instructed
        // to check for such interfaces at marshal time, and marshal classes
        // that implement
        // these interfaces in a different way.
        m.setProperty("org.exolab.castor.xml.proxyInterfaces", "org.hibernate.proxy.HibernateProxy");
        m.setEncoding(prop.getDefaultEncoding());
        // m.setResolver(getXmlContext(mappingFile));
        m.marshal(input);
        String output = baos.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("Finish marshaling input(type" + input.getClass() + ", mappingFile=" + mappingFile + ").");
        }
        return output;
    }

    /**
     * Simply transfer a object into a xml string.
     */
    public static String marshal(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos));
            Marshaller.marshal(object, writer);
            return baos.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * load mapping file from classpath.
     */
    private static synchronized XMLContext getXmlContext(String mappingFile) throws Exception {
        /**
         * Seems like that caching the Mapping instance will result in marshal problem in multi-thread environment. The
         * first thread will parse successfully, then the other thread will lose the mapping information, and use
         * default internal class descriptor. Maybe the internal state of Mappping instance will change, it isn't a
         * read-only instance. BUT the ClassDescriptorResolver instance can be reused.
         */
        // XMLClassDescriptorResolver resolver = mappings.get(mappingFile);
        XMLContext context = mappings.get(mappingFile);
        if (context == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(mappingFile);
            Mapping mapping = new Mapping();
            mapping.loadMapping(new InputSource(is));

            /**
             * I'm trying to use a ClassDescriptorResolver as describe in Castor's Best practices, but I've run into
             * problems. I'm now getting this exception: IllegalStateException: No Introspector defined in properties!
             * thrown by the ByIntrospection class.
             * 
             * http://www.nabble.com/-XML--Problem-using-ClassDescriptorResolver :-Introspector%3D-null-td20702840.html
             * the best practice you've used is outdated for 1.3 you should use something like XMLContext ctx = new
             * XMLContext(); ctx.addMapping(mapping);
             */
            // resolver = (XMLClassDescriptorResolver)
            // ClassDescriptorResolverFactory
            // .createClassDescriptorResolver(BindingType.XML);
            // MappingUnmarshaller mappingUnmarshaller = new
            // MappingUnmarshaller();
            // MappingLoader mappingLoader =
            // mappingUnmarshaller.getMappingLoader(
            // mapping, BindingType.XML);
            // resolver.setMappingLoader(mappingLoader);
            // if (logger.isDebugEnabled()){
            // logger.debug("Create a new ClassDescriptorResolver intance:" +
            // mappingFile);
            // }
            context = new XMLContext();
            context.addMapping(mapping);

            mappings.put(mappingFile, context);
        }
        return context;
    }
}
