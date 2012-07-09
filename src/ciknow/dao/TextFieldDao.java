package ciknow.dao;

import ciknow.domain.TextField;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author gyao
 */
public interface TextFieldDao {

    public void save(TextField textField);

    public void save(Collection<TextField> textFields);

    public TextField findById(Long id);

    public List<TextField> getAll();
}
