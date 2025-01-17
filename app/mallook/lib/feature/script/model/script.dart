class Script {
  Script({
    this.id,
    this.name,
    this.heartCount,
    this.nickname,
    this.imageUrl,
    this.memberGrade,
  });

  Script.fromJson(dynamic json) {
    id = json['id'];
    name = json['name'];
    heartCount = json['heartCount'];
    nickname = json['nickname'];
    imageUrl = json['imageUrl'];
    memberGrade = json['memberGrade'];
  }

  int? id;
  String? name;
  int? heartCount;
  String? nickname;
  String? imageUrl;
  String? memberGrade;

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{};
    map['id'] = id;
    map['name'] = name;
    map['heartCount'] = heartCount;
    map['nickname'] = nickname;
    map['imageUrl'] = imageUrl;
    map['memberGrade'] = memberGrade;
    return map;
  }

  @override
  String toString() {
    return 'Script{id: $id, name: $name, heartCount: $heartCount, nickname: $nickname, imageUrl: $imageUrl}';
  }
}
