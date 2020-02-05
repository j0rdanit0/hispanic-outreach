package org.elpuentesearcy.service;

import com.redfin.sitemapgenerator.GoogleLinkSitemapGenerator;
import com.redfin.sitemapgenerator.GoogleLinkSitemapUrl;
import lombok.RequiredArgsConstructor;
import org.elpuentesearcy.configuration.ElPuenteCaches;
import org.elpuentesearcy.configuration.UrlLocaleResolver;
import org.elpuentesearcy.controller.SearchEngineOptimizationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SitemapService
{
    private static final Logger logger = LoggerFactory.getLogger( SitemapService.class );

    private final ListableBeanFactory listableBeanFactory;
    private final UrlService urlService;

    @Cacheable( cacheNames = ElPuenteCaches.SITEMAP, key = "#subdomain == null ? 'default' : #subdomain.subdomain" )
    public String createSitemap( UrlLocaleResolver.ElPuenteLanguage subdomain ) throws IOException
    {
        GoogleLinkSitemapGenerator sitemap = new GoogleLinkSitemapGenerator( urlService.getBaseUrl( subdomain ), getTempFile() );

        List<String> paths = new ArrayList<>();
        for ( Object controller : listableBeanFactory.getBeansWithAnnotation( Controller.class ).values() )
        {
            if ( !(controller instanceof SearchEngineOptimizationController ) )
            {
                Class<?> klass = controller.getClass();
                while ( klass != Object.class )
                {
                    for ( Method method : klass.getDeclaredMethods() )
                    {
                        if ( method.isAnnotationPresent( GetMapping.class ) )
                        {
                            GetMapping getMapping = method.getAnnotation( GetMapping.class );
                            paths.addAll( Arrays.asList( getMapping.value() ) );
                            paths.addAll( Arrays.asList( getMapping.path() ) );
                        }
                    }

                    klass = klass.getSuperclass();
                }
            }
        }

        for ( String path : paths )
        {
            String url = urlService.getBaseUrl( subdomain ) + path;
            Map<String, Map<String, String>> alternates = new HashMap<>();

            for ( UrlLocaleResolver.ElPuenteLanguage language : UrlLocaleResolver.ElPuenteLanguage.values() )
            {
                if ( language == subdomain )
                {
                    alternates.put( urlService.getBaseUrl() + path, Collections.singletonMap( "hreflang", "en" ) );
                }
                else
                {
                    alternates.put( urlService.getBaseUrl( language ) + path, Collections.singletonMap( "hreflang", language.getSubdomain() ) );
                }
            }

            try
            {
                sitemap.addUrl( new GoogleLinkSitemapUrl( url, alternates ) );
            }
            catch ( URISyntaxException exception )
            {
                logger.error( "Unable to construct URL for sitemap: " + url, exception );
            }
        }

        return slurpFileAndDelete( sitemap.write().get( 0 ) );
    }

    private File getTempFile() throws IOException
    {
        File dir = File.createTempFile( SitemapService.class.getSimpleName(), "" );
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();

        return dir;
    }

    private String slurpFileAndDelete( File file )
    {
        file.deleteOnExit();
        StringBuilder sb = new StringBuilder();
        try
        {
            FileReader reader = new FileReader( file );
            int c;
            while ( ( c = reader.read() ) != -1 )
            {
                sb.append( (char) c );
            }
            reader.close();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        file.delete();
        return sb.toString();
    }
}
