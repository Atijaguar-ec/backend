ALTER TABLE Product DROP CONSTRAINT IF EXISTS FK5414co2yomrmr1wbiahodoi6j;
ALTER TABLE Product DROP COLUMN IF EXISTS specialityDocument_id;
ALTER TABLE Product DROP COLUMN IF EXISTS specialityDescription;

ALTER TABLE ProductLabelContent DROP CONSTRAINT IF EXISTS FKo78331b0piyfvhsjhibs5ckmo;
ALTER TABLE ProductLabelContent DROP COLUMN IF EXISTS specialityDocument_id;
ALTER TABLE ProductLabelContent DROP COLUMN IF EXISTS specialityDescription;
