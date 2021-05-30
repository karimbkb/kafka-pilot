package org.karimbkb.model.kafka.avro;

import com.google.inject.Inject;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.SerializationException;
import org.karimbkb.entity.KafkaConfig;
import org.karimbkb.model.Notification;
import org.karimbkb.model.SchemaHandler;
import org.karimbkb.model.kafka.KafkaCommon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class AvroWriter implements AvroProducer {
  private final KafkaCommon common;
  private final SchemaHandler schemaHandler;

  @Inject
  public AvroWriter(KafkaCommon common, SchemaHandler schemaHandler) {
    this.common = common;
    this.schemaHandler = schemaHandler;
  }

  @Override
  public void produceAvroMessage(String topic, String message, String schemaStr)
      throws SQLException {
    KafkaConfig kafkaConfig = common.getCurrentKafkaConfig();
    try (KafkaProducer<Long, Object> producer =
        new KafkaProducer<>(getAvroProducerProperties(kafkaConfig))) {
      Schema.Parser parser = new Schema.Parser();
      Schema schema = parser.parse(schemaStr);

      DecoderFactory decoderFactory = new DecoderFactory();
      Decoder decoder = decoderFactory.jsonDecoder(schema, message);
      DatumReader<GenericData.Record> reader = new GenericDatumReader<>(schema);
      GenericRecord genericRecord = reader.read(null, decoder);

      ProducerRecord<Long, Object> record = new ProducerRecord<>(topic, genericRecord);
      producer.send(record);
    } catch (SerializationException e) {
      Notification.createExceptionAlert("Error", "Serializing message failed", e).showAndWait();
    } catch (KafkaException e) {
      Notification.createExceptionAlert("Error", "Producing message failed", e).showAndWait();
    } catch (IOException e) {
      Notification.createExceptionAlert("Error", "Schema file reading failed", e).showAndWait();
    }
  }

  @Override
  public String readSchemaFile(String path) throws FileNotFoundException {
    File schemaFile = new File(path);
    Scanner myReader = new Scanner(schemaFile);
    String data = null;

    while (myReader.hasNextLine()) {
      data = myReader.nextLine();
    }
    myReader.close();

    return data;
  }
}
