import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

public class LibraryCalls {

  String badReferenceDereference(Reference ref) {
    return ref.get().toString();
  }

  String badWeakReferenceDereference(WeakReference ref) {
    return ref.get().toString();
  }

  String badPhantomReferenceDereference(PhantomReference ref) {
    return ref.get().toString();
  }

  String badAtomicReferenceDereference(AtomicReference ref) {
    return ref.get().toString();
  }
}
