package XLS;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class XlsUtils {

    public static String getCellValue(Cell cell){
        if(cell == null)
            return "";

        CellType cellType = cell.getCellType();
        if (cellType.FORMULA == cellType)
            cellType = cell.getCachedFormulaResultType();

        if (cellType.NUMERIC == cellType) {
            Double d = new Double(cell.getNumericCellValue());
            return String.valueOf(d);
        }
        else if (cellType.STRING == cellType)
            return cell.getStringCellValue();
        else if (cellType.BOOLEAN == cellType)
            return String.valueOf(cell.getBooleanCellValue());
        else if (cellType.BLANK == cellType)
            return "";
        else
            return "";

    }

}
