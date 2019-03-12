// .//GEN-BEGIN:_1_be
/******************************************************
 * Code Generated From JAFFA Framework Default Pattern
 *
 * The JAFFA Project can be found at http://jaffa.sourceforge.net
 * and is available under the Lesser GNU Public License
 ******************************************************/
package org.jaffa.modules.printing.components.printerdefinitionfinder.tx;

import java.util.*;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jaffa.exceptions.ApplicationExceptions;
import org.jaffa.exceptions.FrameworkException;
import org.jaffa.persistence.UOW;
import org.jaffa.persistence.exceptions.UOWException;
import org.jaffa.persistence.Criteria;
import org.jaffa.persistence.AtomicCriteria;
import org.jaffa.components.finder.CriteriaField;
import org.jaffa.components.finder.OrderByField;
import org.jaffa.components.finder.FinderTx;

import org.jaffa.modules.printing.components.printerdefinitionfinder.IPrinterDefinitionFinder;
import org.jaffa.modules.printing.components.printerdefinitionfinder.dto.PrinterDefinitionFinderInDto;
import org.jaffa.modules.printing.components.printerdefinitionfinder.dto.PrinterDefinitionFinderOutDto;
import org.jaffa.modules.printing.components.printerdefinitionfinder.dto.PrinterDefinitionFinderOutRowDto;
import org.jaffa.modules.printing.domain.PrinterDefinition;
import org.jaffa.modules.printing.domain.PrinterDefinitionMeta;


// .//GEN-END:_1_be
// Add additional imports//GEN-FIRST:_imports




// .//GEN-LAST:_imports
// .//GEN-BEGIN:_2_be
/** Finder for PrinterDefinition objects.
*/
public class PrinterDefinitionFinderTx implements IPrinterDefinitionFinder {

    private static Logger log = Logger.getLogger(PrinterDefinitionFinderTx.class);

    // .//GEN-END:_2_be
    // .//GEN-BEGIN:_destroy_1_be
    /**
     * This should be invoked, when done with the component.
     */
    public void destroy() {
        // .//GEN-END:_destroy_1_be
        // Add custom code //GEN-FIRST:_destroy_1


        // .//GEN-LAST:_destroy_1
        // .//GEN-BEGIN:_destroy_2_be
    }
    // .//GEN-END:_destroy_2_be
    // .//GEN-BEGIN:_find_1_be
    /** Searches for PrinterDefinition objects.
     * @param input The criteria based on which the search will be performed.
     * @throws ApplicationExceptions This will be thrown if the criteria contains invalid data.
     * @throws FrameworkException Indicates some system error
     * @return The search results.
     */
    public PrinterDefinitionFinderOutDto find(PrinterDefinitionFinderInDto input)
    throws FrameworkException, ApplicationExceptions {
        UOW uow = null;
        try {
            // Print Debug Information for the input
            if (log.isDebugEnabled()) {
                log.debug("Input: " + input);
            }

            // create the UOW
            uow = new UOW();

            // Build the Criteria Object
            Criteria criteria = buildCriteria(input, uow);
            // .//GEN-END:_find_1_be
            // Add custom code before the query //GEN-FIRST:_find_1


            // .//GEN-LAST:_find_1
            // .//GEN-BEGIN:_find_2_be
            // Execute The Query
            Collection results = uow.query(criteria);
            // .//GEN-END:_find_2_be
            // Add custom code after the query //GEN-FIRST:_find_2


            // .//GEN-LAST:_find_2
            // .//GEN-BEGIN:_find_3_be
            // Convert the domain objects into the outbound dto
            PrinterDefinitionFinderOutDto output = buildDto(uow, results, input);

            // Find the total number of records
            FinderTx.findTotalRecords(uow, input, criteria, output, output.getRowsCount());

            // Print Debug Information for the output
            if (log.isDebugEnabled()) {
                log.debug("Output: " + output);
            }

            return output;

        } finally {
            if (uow != null)
                uow.rollback();
        }
    }
    // .//GEN-END:_find_3_be
    // .//GEN-BEGIN:_buildCriteria_1_be
    private Criteria buildCriteria(PrinterDefinitionFinderInDto input, UOW uow) {

        Criteria criteria = new Criteria();
        criteria.setTable( PrinterDefinitionMeta.getName() );
        // .//GEN-END:_buildCriteria_1_be
        // Add custom criteria //GEN-FIRST:_buildCriteria_1


        // .//GEN-LAST:_buildCriteria_1
        // .//GEN-BEGIN:_buildCriteria_2_be
        FinderTx.addCriteria(input.getPrinterId(), PrinterDefinitionMeta.PRINTER_ID, criteria);
        FinderTx.addCriteria(input.getDescription(), PrinterDefinitionMeta.DESCRIPTION, criteria);
        FinderTx.addCriteria(input.getSiteCode(), PrinterDefinitionMeta.SITE_CODE, criteria);
        FinderTx.addCriteria(input.getLocationCode(), PrinterDefinitionMeta.LOCATION_CODE, criteria);
        FinderTx.addCriteria(input.getRealPrinterName(), PrinterDefinitionMeta.REAL_PRINTER_NAME, criteria);
        FinderTx.addCriteria(input.getOutputType(), PrinterDefinitionMeta.OUTPUT_TYPE, criteria);
        FinderTx.addCriteria(input.getScaleToPageSize(), PrinterDefinitionMeta.SCALE_TO_PAGE_SIZE, criteria);
        FinderTx.addCriteria(input.getRemote(), PrinterDefinitionMeta.REMOTE, criteria);


        // append an orderBy clause to the criteria
        OrderByField[] orderByFields = input.getOrderByFields();
        if (orderByFields != null) {
            for (int i = 0; i < orderByFields.length; i++) {
                OrderByField orderByField = orderByFields[i];
                int sort = Criteria.ORDER_BY_ASC;
                if (orderByField.getSortAscending() != null && !orderByField.getSortAscending().booleanValue() )
                    sort = Criteria.ORDER_BY_DESC;
                criteria.addOrderBy(orderByField.getFieldName(), sort);
            }
        }

        // limit the output
        criteria.setFirstResult(input.getFirstRecord());
        criteria.setMaxResults(input.getMaxRecords());

        // .//GEN-END:_buildCriteria_2_be
        // Add custom criteria //GEN-FIRST:_buildCriteria_2


        // .//GEN-LAST:_buildCriteria_2
        // .//GEN-BEGIN:_buildCriteria_3_be
        return criteria;
    }
    // .//GEN-END:_buildCriteria_3_be
    // .//GEN-BEGIN:_buildDto_1_be
    private PrinterDefinitionFinderOutDto buildDto(UOW uow, Collection results, PrinterDefinitionFinderInDto input)
    throws UOWException {

        PrinterDefinitionFinderOutDto output = new PrinterDefinitionFinderOutDto();



        for (Iterator i = results.iterator(); i.hasNext();) {
            PrinterDefinitionFinderOutRowDto row = new PrinterDefinitionFinderOutRowDto();
            PrinterDefinition printerDefinition = (PrinterDefinition) i.next();
            // .//GEN-END:_buildDto_1_be
            // Add custom code before all the setters //GEN-FIRST:_buildDto_1


            // .//GEN-LAST:_buildDto_1
            // .//GEN-BEGIN:_buildDto_PrinterId_1_be
            row.setPrinterId(printerDefinition.getPrinterId());
            // .//GEN-END:_buildDto_PrinterId_1_be
            // .//GEN-BEGIN:_buildDto_Description_1_be
            row.setDescription(printerDefinition.getDescription());
            // .//GEN-END:_buildDto_Description_1_be
            // .//GEN-BEGIN:_buildDto_SiteCode_1_be
            row.setSiteCode(printerDefinition.getSiteCode());
            // .//GEN-END:_buildDto_SiteCode_1_be
            // .//GEN-BEGIN:_buildDto_LocationCode_1_be
            row.setLocationCode(printerDefinition.getLocationCode());
            // .//GEN-END:_buildDto_LocationCode_1_be
            // .//GEN-BEGIN:_buildDto_Remote_1_be
            row.setRemote(printerDefinition.getRemote());
            // .//GEN-END:_buildDto_Remote_1_be
            // .//GEN-BEGIN:_buildDto_RealPrinterName_1_be
            row.setRealPrinterName(printerDefinition.getRealPrinterName());
            // .//GEN-END:_buildDto_RealPrinterName_1_be
            // .//GEN-BEGIN:_buildDto_OutputType_1_be
            row.setOutputType(printerDefinition.getOutputType());
            // .//GEN-END:_buildDto_OutputType_1_be
            // .//GEN-BEGIN:_buildDto_ScaleToPageSize_1_be
            row.setScaleToPageSize(printerDefinition.getScaleToPageSize());
            // .//GEN-END:_buildDto_ScaleToPageSize_1_be

            // Add custom code to pass values to the dto //GEN-FIRST:_buildDto_2


            // .//GEN-LAST:_buildDto_2
            // .//GEN-BEGIN:_buildDto_3_be
            output.addRows(row);
        }
        return output;
    }
    // .//GEN-END:_buildDto_3_be
    // All the custom code goes here //GEN-FIRST:_custom






    // .//GEN-LAST:_custom
}
