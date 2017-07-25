package org.opendatakit.services.forms;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.database.DatabaseConstants;
import org.opendatakit.database.service.DbHandle;
import org.opendatakit.provider.FormsColumns;
import org.opendatakit.services.database.AndroidConnectFactory;
import org.opendatakit.services.database.OdkConnectionFactorySingleton;
import org.opendatakit.services.database.OdkConnectionInterface;
import org.opendatakit.services.forms.provider.FormsProvider;
import org.opendatakit.services.forms.provider.FormsProviderTest;
import org.opendatakit.utilities.LocalizationUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.opendatakit.services.forms.provider.FormsProviderTest.getAppName;

/**
 * Created by Niles on 6/29/17.
 */

@RunWith(AndroidJUnit4.class)
public class FormInfoTest {
  private FormInfo info;

  private static String join(String s, String[] arr) {
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (String item : arr) {
      if (first) {
        first = false;
      } else {
        b.append(s);
      }
      b.append(item);
    }
    return b.toString();
  }

  @Test
  public void testCursorConstructor() throws Throwable {
    AndroidConnectFactory.configure();
    DbHandle uniqueKey = new DbHandle(
        getClass().getSimpleName() + AndroidConnectFactory.INTERNAL_TYPE_SUFFIX);
    OdkConnectionInterface db = OdkConnectionFactorySingleton.getOdkConnectionFactoryInterface()
        .getConnection("default", uniqueKey);
    insert("breathcounter");
    Cursor c = db.rawQuery("SELECT " + join(", ", FormsColumns.formsDataColumnNames) + " FROM "
            + DatabaseConstants.FORMS_TABLE_NAME + " WHERE " + FormsColumns.FORM_ID + " =?",
        new String[] { "breathcounter" });
    c.moveToFirst();
    info = new FormInfo("default", c, false);
    assertEquals(info.tableId, "breathcounter");
    assertEquals(info.formVersion, "20130408");
    assertEquals(info.formDef, null);
  }

  @Before
  public void setUp() {
    setUp("default", "Tea_houses_editable");
  }

  public void setUp(String app, String form) {
    insert(form);
    info = new FormInfo(null, "default", new File(
        Environment.getExternalStorageDirectory().getPath() + "/opendatakit/" + app
            + "/config/tables/" + form + "/forms" + "/" + form + "/formDef.json"));
  }

  public void insert(String form) {
    try {
      new FormsProviderTest().setUp();
      // make sure it's in the database when we try to query it
      new FormsProvider().insert(new Uri.Builder().appendPath(getAppName()).build(),
          FormsProviderTest.getCvs(form));
    } catch (Throwable ignored) {
      // ignore
    }
  }

  @Test
  public void testAsRowValues() {
    String[] res = info.asRowValues(new String[] { "displayName", "defaultFormLocale", "tableId" });
    String local = LocalizationUtils.getLocalizedDisplayName("default", res[2], res[1], res[0]);
    assertEquals(local, "Tea Houses Editable");
    assertEquals(res[1], "default");
    assertEquals(res[2], "Tea_houses_editable");
  }

  @Test
  public void testProperties() {
    assertEquals(info.appName, "default");
    assertEquals(info.defaultLocale, "default");
    assertEquals(info.formId, "Tea_houses_editable");
    assertEquals(info.formVersion, "20140204");
    setUp("default", "twoColumn");
    assertEquals(info.appName, "default");
    assertEquals(info.defaultLocale, "default");
    assertEquals(info.formId, "twoColumn");
    assertEquals(info.formVersion, "20130408");
    assertEquals(info.instanceName, null);
    assertEquals(info.formTitle, "{\"text\":\"Two Column Form\"}");
    setUp("default", "complex_validate_test");
    assertEquals(info.appName, "default");
    assertEquals(info.defaultLocale, "default");
    assertEquals(info.tableId, "complex_validate_test");
    assertEquals(info.formId, "complex_validate_test");
    assertEquals(info.formVersion, "20130808");
    assertEquals(info.instanceName, null);
    assertEquals(info.formTitle, "{\"text\":{\"default\":\"Two Part Validation Test\","
        + "\"hindi\":\"दो भाग मान्यकरण टेस्ट\"}}");
    assertEquals(
        LocalizationUtils.getLocalizedDisplayName("default", info.tableId, "hindi", info.formTitle),
        "दो भाग मान्यकरण टेस्ट");
    assertEquals(LocalizationUtils
            .getLocalizedDisplayName("default", info.tableId, "default", info.formTitle),
        "Two Part Validation Test");
  }

}
