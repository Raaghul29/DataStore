# Data Store



## Installation

Use the jar as local dependency to use the above mentioned functionalities.

```bash
mvn install
```

## Usage

```java
import com.api.datastorage.DataStore;

public class test{
  DataStore dataStore = new DataStore();

  dataStore.store(arg0, arg1);
  dataStore.store(arg0, arg1, arg2, arg3);
  dataStore.store(arg0, arg1, arg3);
  dataStore.read(arg0);
  dataStore.delete(arg0);
}