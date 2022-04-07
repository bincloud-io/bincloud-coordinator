package io.bcs.fileserver.domain.model.storage;

/**
 * This interface describes the content fragment.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ContentFragment {
  public Long getOffset();

  public Long getLength();
}
