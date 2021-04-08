import 'package:dart_json_mapper/dart_json_mapper.dart';

// @Json(valueDecorators:  $name.valueDecorators)
@JsonSerializable()
class $name with ChangeNotifier {
  ///定义List的解析器，供上面Json参数使用，所有List或者Array都需要
  // static Map<Type, ValueDecoratorFunction> valueDecorators() => {
  // };
  $name();
}