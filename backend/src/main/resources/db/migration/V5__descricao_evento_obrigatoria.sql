-- O enunciado exige que todo evento tenha descrição. Eventos antigos sem descrição
-- recebem o próprio nome, para a restrição NOT NULL poder ser aplicada.
UPDATE evento SET descricao = nome WHERE descricao IS NULL OR btrim(descricao) = '';

ALTER TABLE evento ALTER COLUMN descricao SET NOT NULL;
